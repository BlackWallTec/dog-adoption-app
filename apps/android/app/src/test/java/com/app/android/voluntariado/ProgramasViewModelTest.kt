package com.app.android.voluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.dtos.dtosVoluntariado.ProgramasResponseDto
import com.app.android.data.repoImp.repoImpVoluntariado.ProgramaRepositoryImpl
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerProgramasVigentesUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.toUi
import com.google.gson.Gson
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.Instant
import kotlin.coroutines.cancellation.CancellationException

/*
 * PRUEBAS DE ProgramasViewModel: EL FLUJO REAL DE LA APP
 *
 * `ProgramasScreen` usa este ViewModel: carga la lista y atiende el botón "Inscribirme".
 * Comunica sus resultados de estas formas:
 *   - `uiState`: lo que la pantalla dibuja (cargando, vacía, lista de ProgramaUi o error).
 *   - `mensajes`: los textos de error del snackbar, que se muestran una sola vez.
 *   - `inscripcionExitosa`: evento de una sola vez para ir a la pantalla de confirmación.
 *   - `programaConfirmado`: el ProgramaUi recién inscrito que lee esa confirmación.
 *
 * El ViewModel recibe un Clock: usamos RELOJ_FIJO para que "ahora" sea siempre AHORA.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ProgramasViewModelTest {
    // Fake del catálogo de programas. `compuerta` simula una red lenta, igual que en
    // RepositorioFalso, y `falla` simula que el servidor no responde.
    private class CatalogoFalso(
        var programas: List<Programa> = listOf(programaValido()),
        var falla: Exception? = null,
        var compuerta: CompletableDeferred<Unit>? = null,
    ) : ProgramaRepository {
        var llamadas = 0

        override suspend fun obtenerProgramasVigentes(): List<Programa> {
            llamadas++
            compuerta?.await()
            falla?.let { throw it }
            return programas
        }

        override suspend fun obtenerMisProgramas(): List<Programa> = programas.filter { it.usuarioInscrito }
    }

    @Before
    fun instalarHiloPrincipalDePrueba() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun quitarHiloPrincipalDePrueba() {
        Dispatchers.resetMain()
    }

    private fun viewModel(
        catalogo: ProgramaRepository = CatalogoFalso(),
        inscripciones: InscripcionRepository = RepositorioFalso(),
    ) = ProgramasViewModel(
        ObtenerProgramasVigentesUseCase(catalogo),
        InscribirseProgramaUseCase(inscripciones, RELOJ_FIJO),
        CancelarInscripcionUseCase(inscripciones, RELOJ_FIJO),
        RELOJ_FIJO,
    )

    /*
     * `mensajes` es un SharedFlow: si nadie está escuchando cuando se emite un mensaje,
     * ese mensaje se pierde. Por eso primero empezamos a escuchar, después ejecutamos la
     * acción y al final dejamos de escuchar y regresamos lo que llegó.
     */
    private fun mensajesDurante(vm: ProgramasViewModel, accion: () -> Unit): List<String> =
        runBlocking {
            val recibidos = mutableListOf<String>()
            val escucha = launch(UnconfinedTestDispatcher()) { vm.mensajes.toList(recibidos) }
            accion()
            escucha.cancel()
            recibidos
        }

    // Mismo patrón con `inscripcionExitosa`: solo importa cuántas veces llegó.
    private fun exitosDurante(vm: ProgramasViewModel, accion: () -> Unit): Int =
        runBlocking {
            val recibidos = mutableListOf<Unit>()
            val escucha = launch(UnconfinedTestDispatcher()) { vm.inscripcionExitosa.toList(recibidos) }
            accion()
            escucha.cancel()
            recibidos.size
        }

    private fun tarjetas(vm: ProgramasViewModel): List<ProgramaUi> =
        (vm.uiState.value as ProgramasUiState.Success).programas

    // ------------------------------------------------------------------------------
    // CARGA DE LA LISTA
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - con programas muestra las tarjetas ya convertidas a ProgramaUi`() {
        val vm = viewModel(CatalogoFalso(listOf(programaValido())))

        assertEquals(ProgramasUiState.Success(listOf(programaValido().toUi(AHORA))), vm.uiState.value)
    }

    @Test
    fun `estructural - la tarjeta trae el cupo en tiempo real`() {
        val programa = programaValido().copy(cupoMaximo = 15, cupoDisponible = 7)
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        val tarjeta = tarjetas(vm).single()
        assertEquals("8/15 lugares ocupados · 7 disponibles", tarjeta.textoCupo)
        assertEquals("Inscribirme", tarjeta.textoBoton)
        assertTrue(tarjeta.botonHabilitado)
    }

    @Test
    fun `estructural - usa el reloj inyectado para decidir si ya comenzo`() {
        // Con la hora real (2026-11-15 o después) este programa ya habría comenzado; con el
        // reloj fijo todavía no.
        val programa = programaValido().copy(fechaInicio = AHORA.plusSeconds(1))
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        assertEquals("Inscribirme", tarjetas(vm).single().textoBoton)
    }

    @Test
    fun `vacios - sin programas muestra el estado vacio`() {
        val vm = viewModel(CatalogoFalso(emptyList()))

        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `rangos - carga listas de distintos tamanos`() {
        for (tamano in listOf(1, 2, 10, 1000)) {
            val programas = List(tamano) { programaValido().copy(id = "programa-$it") }

            val vm = viewModel(CatalogoFalso(programas))

            assertEquals("tamano=$tamano", tamano, tarjetas(vm).size)
        }
    }

    @Test
    fun `estructural - mientras carga muestra Loading`() {
        val compuerta = CompletableDeferred<Unit>()

        val vm = viewModel(CatalogoFalso(compuerta = compuerta))
        assertEquals(ProgramasUiState.Loading, vm.uiState.value)

        compuerta.complete(Unit)
        assertTrue(vm.uiState.value is ProgramasUiState.Success)
    }

    @Test
    fun `estructural - si falla la carga muestra error y se puede reintentar`() {
        val catalogo = CatalogoFalso(falla = IOException("sin red"))
        val vm = viewModel(catalogo)

        assertEquals(ProgramasUiState.Error("No se pudieron cargar los programas"), vm.uiState.value)

        catalogo.falla = null
        vm.cargarProgramas()

        assertTrue(vm.uiState.value is ProgramasUiState.Success)
    }

    // ------------------------------------------------------------------------------
    // INSCRIBIRSE: CAMINO FELIZ
    // ------------------------------------------------------------------------------

    @Test
    fun `exito - la tarjeta queda con Cancelar y un lugar menos y va a la confirmacion`() {
        // programaValido(): cupoMaximo 10, cupoDisponible 5 -> 5/10 ocupados.
        val otro = programaValido().copy(id = "programa-2")
        val inscripciones = RepositorioFalso()
        val vm = viewModel(CatalogoFalso(listOf(programaValido(), otro)), inscripciones)

        val exitos = exitosDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(1, exitos)
        assertEquals(listOf("programa-1"), inscripciones.llamadas.toList())

        val tarjeta = tarjetas(vm)[0]
        assertEquals("Cancelar", tarjeta.textoBoton)
        assertTrue(tarjeta.botonHabilitado) // HU37: inscrito y sin iniciar -> puede cancelar
        assertEquals("6/10 lugares ocupados · 4 disponibles", tarjeta.textoCupo)

        // La confirmación recibe la misma tarjeta ya actualizada.
        assertEquals(tarjeta, vm.programaConfirmado.value)

        // Las demás tarjetas no cambian.
        assertEquals(otro.toUi(AHORA), tarjetas(vm)[1])
    }

    @Test
    fun `exito - el ultimo lugar deja la tarjeta llena`() {
        val programa = programaValido().copy(cupoMaximo = 10, cupoDisponible = 1)
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        exitosDurante(vm) { vm.inscribirse(programa.id) }

        assertEquals("10/10 lugares ocupados · 0 disponibles", tarjetas(vm)[0].textoCupo)
        assertEquals(1f, tarjetas(vm)[0].progresoCupo, 0f)
        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)
    }

    @Test
    fun `exito - no vuelve a pedir la lista al servidor`() {
        val catalogo = CatalogoFalso()
        val vm = viewModel(catalogo)

        exitosDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(1, catalogo.llamadas)
    }

    @Test
    fun `nulos - con cupo sin limite tambien se puede inscribir`() {
        val programa = programaValido().copy(cupoMaximo = null, cupoDisponible = null)
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        exitosDurante(vm) { vm.inscribirse(programa.id) }

        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)
        assertEquals("Sin límite de lugares", tarjetas(vm)[0].textoCupo)
    }

    @Test
    fun `vacios - un nombre vacio no rompe la confirmacion`() {
        val programa = programaValido().copy(nombre = "")
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        val exitos = exitosDurante(vm) { vm.inscribirse(programa.id) }

        assertEquals(1, exitos)
        assertEquals("", vm.programaConfirmado.value!!.titulo)
    }

    @Test
    fun `nulos - antes de inscribirse no hay programa confirmado`() {
        assertNull(viewModel().programaConfirmado.value)
    }

    // ------------------------------------------------------------------------------
    // INSCRIBIRSE: ERRORES DEL SERVIDOR
    // ------------------------------------------------------------------------------

    @Test
    fun `SinCupo - recarga la lista sin pasar por Loading y avisa`() {
        val compuerta = CompletableDeferred<Unit>()
        val catalogo = CatalogoFalso()
        val inscripciones = RepositorioFalso(respuesta = Resultado.Error(VoluntariadoError.SinCupo))
        val vm = viewModel(catalogo, inscripciones)
        assertEquals(1, catalogo.llamadas)

        // Mientras tanto, alguien más ocupó el último lugar.
        catalogo.programas = listOf(programaValido().copy(cupoDisponible = 0))
        catalogo.compuerta = compuerta

        val estados = mutableListOf<ProgramasUiState>()
        val mensajes =
            mensajesDurante(vm) {
                runBlocking {
                    val escucha = launch(UnconfinedTestDispatcher()) { vm.uiState.toList(estados) }
                    vm.inscribirse("programa-1")

                    // La recarga está "en camino": la lista sigue visible, no hay Loading.
                    assertEquals(2, catalogo.llamadas)
                    assertTrue(vm.uiState.value is ProgramasUiState.Success)
                    assertEquals("Inscribiendo…", tarjetas(vm)[0].textoBoton)

                    compuerta.complete(Unit)
                    escucha.cancel()
                }
            }

        assertEquals(listOf("Este programa ya no tiene lugares disponibles."), mensajes)
        assertFalse(estados.any { it is ProgramasUiState.Loading })
        // La tarjeta refleja el cupo real que mandó el servidor.
        assertEquals("Sin lugares", tarjetas(vm)[0].textoBoton)
        assertEquals("10/10 lugares ocupados · 0 disponibles", tarjetas(vm)[0].textoCupo)
        assertNull(vm.programaConfirmado.value)
    }

    @Test
    fun `SinCupo - si la recarga falla se queda la lista actual y aun asi avisa`() {
        val catalogo = CatalogoFalso()
        val inscripciones = RepositorioFalso(respuesta = Resultado.Error(VoluntariadoError.SinCupo))
        val vm = viewModel(catalogo, inscripciones)
        catalogo.falla = IOException("sin red")

        val mensajes = mensajesDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(listOf("Este programa ya no tiene lugares disponibles."), mensajes)
        assertEquals(listOf(programaValido().toUi(AHORA)), tarjetas(vm))
    }

    @Test
    fun `YaInscrito - la tarjeta queda con Cancelar sin cambiar el cupo`() {
        val respuesta = Resultado.Error(VoluntariadoError.YaInscrito)
        val catalogo = CatalogoFalso()
        val vm = viewModel(catalogo, RepositorioFalso(respuesta = respuesta))

        val mensajes = mensajesDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(listOf("Ya estás inscrito en este programa."), mensajes)
        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)
        assertTrue(tarjetas(vm)[0].botonHabilitado)
        assertEquals("5/10 lugares ocupados · 5 disponibles", tarjetas(vm)[0].textoCupo)
        assertEquals(1, catalogo.llamadas) // YaInscrito no recarga
        assertNull(vm.programaConfirmado.value)
    }

    @Test
    fun `estructural - cada error muestra su mensaje`() {
        val esperados =
            mapOf(
                VoluntariadoError.SinCupo to "Este programa ya no tiene lugares disponibles.",
                VoluntariadoError.YaInscrito to "Ya estás inscrito en este programa.",
                VoluntariadoError.ProgramaIniciado to "Este programa ya comenzó, ya no es posible inscribirse.",
                VoluntariadoError.ProgramaNoDisponible to "Este programa no está disponible por ahora.",
                VoluntariadoError.ProgramaNoEncontrado to "No encontramos este programa. Actualiza la lista.",
                VoluntariadoError.NoAutenticado to "Inicia sesión para inscribirte.",
                VoluntariadoError.SinConexion to "Sin conexión. Revisa tu internet e inténtalo de nuevo.",
                VoluntariadoError.Desconocido to "Ocurrió un error. Inténtalo de nuevo.",
            )
        assertEquals(TODOS_LOS_ERRORES.toSet(), esperados.keys)

        for ((error, mensaje) in esperados) {
            val vm = viewModel(inscripciones = RepositorioFalso(respuesta = Resultado.Error(error)))

            val mensajes = mensajesDurante(vm) { vm.inscribirse("programa-1") }

            assertEquals("error=$error", listOf(mensaje), mensajes)
            // Solo YaInscrito marca la tarjeta (con Cancelar); los demás dejan el botón listo para reintentar
            // (SinCupo recarga la misma lista del fake, que todavía tiene lugares).
            val esperadoBoton = if (error == VoluntariadoError.YaInscrito) "Cancelar" else "Inscribirme"
            assertEquals("error=$error", esperadoBoton, tarjetas(vm)[0].textoBoton)
        }
    }

    // ------------------------------------------------------------------------------
    // INSCRIBIRSE: DOBLE TOQUE, IDS Y LÍMITES
    // ------------------------------------------------------------------------------

    @Test
    fun `doble toque - mientras se envia el caso de uso se llama una sola vez`() {
        val compuerta = CompletableDeferred<Unit>()
        val inscripciones = RepositorioFalso(compuerta = compuerta)
        val otro = programaValido().copy(id = "programa-2")
        val vm = viewModel(CatalogoFalso(listOf(programaValido(), otro)), inscripciones)

        val exitos =
            exitosDurante(vm) {
                vm.inscribirse("programa-1")

                // Mientras viaja: el botón dice "Inscribiendo…" y está deshabilitado.
                assertEquals("Inscribiendo…", tarjetas(vm)[0].textoBoton)
                assertFalse(tarjetas(vm)[0].botonHabilitado)
                assertEquals("Inscribirme", tarjetas(vm)[1].textoBoton)

                // Estrés: 100 toques más (y uno en otro programa) no deben hacer nada.
                repeat(100) { vm.inscribirse("programa-1") }
                vm.inscribirse("programa-2")

                compuerta.complete(Unit)
            }

        assertEquals(listOf("programa-1"), inscripciones.llamadas.toList())
        assertEquals(1, exitos)
        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)
    }

    @Test
    fun `estructural - al terminar un envio se puede volver a intentar`() {
        val inscripciones = RepositorioFalso(respuesta = Resultado.Error(VoluntariadoError.SinConexion))
        val vm = viewModel(inscripciones = inscripciones)

        val mensajes =
            mensajesDurante(vm) {
                vm.inscribirse("programa-1")
                vm.inscribirse("programa-1")
            }

        assertEquals(2, inscripciones.llamadas.size)
        assertEquals(2, mensajes.size)
        assertEquals("Inscribirme", tarjetas(vm)[0].textoBoton)
    }

    @Test
    fun `estructural - una cancelacion libera el boton gracias al finally`() {
        var intentos = 0
        val inscripciones =
            object : InscripcionRepository {
                override suspend fun inscribirse(idPrograma: String): Resultado<Inscripcion> {
                    intentos++
                    if (intentos == 1) throw CancellationException("cancelada")
                    return Resultado.Exito(INSCRIPCION_OK)
                }

                override suspend fun cancelarInscripcion(idPrograma: String): Resultado<Inscripcion> =
                    Resultado.Exito(INSCRIPCION_OK)
            }
        val vm = viewModel(inscripciones = inscripciones)

        val exitos =
            exitosDurante(vm) {
                vm.inscribirse("programa-1")
                // Tras la cancelación, el botón ya no dice "Inscribiendo…".
                assertEquals("Inscribirme", tarjetas(vm)[0].textoBoton)
                vm.inscribirse("programa-1")
            }

        assertEquals(2, intentos)
        assertEquals(1, exitos)
    }

    @Test
    fun `id inexistente - no llama al caso de uso ni cambia la pantalla`() {
        val inscripciones = RepositorioFalso()
        val vm = viewModel(inscripciones = inscripciones)
        val antes = vm.uiState.value

        for (id in listOf("no-existe", "", "PROGRAMA-1", " programa-1")) {
            val exitos = exitosDurante(vm) { vm.inscribirse(id) }
            assertEquals("id='$id'", 0, exitos)
        }

        assertTrue(inscripciones.llamadas.isEmpty())
        assertEquals(antes, vm.uiState.value)
        assertNull(vm.programaConfirmado.value)
    }

    @Test
    fun `id inexistente - con la lista vacia tampoco hace nada`() {
        val inscripciones = RepositorioFalso()
        val vm = viewModel(CatalogoFalso(emptyList()), inscripciones)

        val exitos = exitosDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(0, exitos)
        assertTrue(inscripciones.llamadas.isEmpty())
        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `limites - cupo y fecha en la frontera`() {
        val casos =
            listOf(
                programaValido().copy(cupoDisponible = 0) to "Sin lugares",
                programaValido().copy(cupoDisponible = 1) to "Cancelar",
                programaValido().copy(fechaInicio = AHORA) to "Ya comenzó",
                programaValido().copy(fechaInicio = AHORA.plusNanos(1)) to "Cancelar",
            )

        for ((programa, botonFinal) in casos) {
            val inscripciones = RepositorioFalso()
            val vm = viewModel(CatalogoFalso(listOf(programa)), inscripciones)

            mensajesDurante(vm) { vm.inscribirse(programa.id) }

            val llegaAlServidor = botonFinal == "Cancelar"
            assertEquals("$programa", llegaAlServidor, inscripciones.llamadas.isNotEmpty())
            assertEquals("$programa", botonFinal, tarjetas(vm)[0].textoBoton)
        }
    }

    @Test
    fun `rangos - cien programas distintos solo marcan el elegido`() {
        val programas = List(100) { programaValido().copy(id = "programa-$it") }
        val vm = viewModel(CatalogoFalso(programas))

        mensajesDurante(vm) { vm.inscribirse("programa-42") }

        val marcados = tarjetas(vm).filter { it.textoBoton == "Cancelar" }.map { it.id }
        assertEquals(listOf("programa-42"), marcados)
    }

    // ------------------------------------------------------------------------------
    // CANCELAR INSCRIPCIÓN (HU37) JUNTO CON EL CUPO (HU39)
    // ------------------------------------------------------------------------------

    private fun cancelacionesDurante(vm: ProgramasViewModel, accion: () -> Unit): Int =
        runBlocking {
            val recibidos = mutableListOf<Unit>()
            val escucha = launch(UnconfinedTestDispatcher()) { vm.cancelacionExitosa.toList(recibidos) }
            accion()
            escucha.cancel()
            recibidos.size
        }

    @Test
    fun `cancelar - exito libera el lugar, regresa a Inscribirme y avisa`() {
        // Inscrito: 6/10 ocupados, 4 disponibles
        val programa = programaValido().copy(usuarioInscrito = true, cupoDisponible = 4)
        val inscripciones = RepositorioFalso()
        val vm = viewModel(CatalogoFalso(listOf(programa)), inscripciones)
        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)

        var cancelaciones = 0
        val mensajes = mensajesDurante(vm) { cancelaciones = cancelacionesDurante(vm) { vm.cancelarInscripcion(programa.id) } }

        assertEquals(1, cancelaciones)
        assertEquals(listOf(programa.id), inscripciones.cancelaciones.toList())
        assertEquals(listOf("Cancelaste tu inscripción en ${programa.nombre}."), mensajes)
        assertEquals("Inscribirme", tarjetas(vm)[0].textoBoton)
        assertEquals("5/10 lugares ocupados · 5 disponibles", tarjetas(vm)[0].textoCupo)
    }

    @Test
    fun `cancelar - despues de cancelar se puede volver a inscribir`() {
        val vm = viewModel()

        exitosDurante(vm) { vm.inscribirse("programa-1") }
        assertEquals("6/10 lugares ocupados · 4 disponibles", tarjetas(vm)[0].textoCupo)
        cancelacionesDurante(vm) { vm.cancelarInscripcion("programa-1") }
        assertEquals("5/10 lugares ocupados · 5 disponibles", tarjetas(vm)[0].textoCupo)
        val exitos = exitosDurante(vm) { vm.inscribirse("programa-1") }

        assertEquals(1, exitos)
        assertEquals("Cancelar", tarjetas(vm)[0].textoBoton)
        assertEquals("6/10 lugares ocupados · 4 disponibles", tarjetas(vm)[0].textoCupo)
    }

    @Test
    fun `cancelar - mientras viaja dice Cancelando y bloquea el doble toque`() {
        val compuerta = CompletableDeferred<Unit>()
        val programa = programaValido().copy(usuarioInscrito = true)
        val inscripciones = RepositorioFalso(compuerta = compuerta)
        val vm = viewModel(CatalogoFalso(listOf(programa, programaValido().copy(id = "programa-2"))), inscripciones)

        cancelacionesDurante(vm) {
            vm.cancelarInscripcion(programa.id)

            assertEquals("Cancelando…", tarjetas(vm)[0].textoBoton)
            assertFalse(tarjetas(vm)[0].botonHabilitado)

            // Ni otra cancelación ni una inscripción en otra tarjeta pasan mientras tanto
            repeat(10) { vm.cancelarInscripcion(programa.id) }
            vm.inscribirse("programa-2")

            compuerta.complete(Unit)
        }

        assertEquals(listOf(programa.id), inscripciones.cancelaciones.toList())
        assertTrue(inscripciones.llamadas.isEmpty())
    }

    @Test
    fun `cancelar - NoInscrito recarga el estado real y avisa`() {
        val programa = programaValido().copy(usuarioInscrito = true)
        val catalogo = CatalogoFalso(listOf(programa))
        val inscripciones = RepositorioFalso(respuestaCancelacion = Resultado.Error(VoluntariadoError.NoInscrito))
        val vm = viewModel(catalogo, inscripciones)
        // En el servidor ya no estaba inscrito
        catalogo.programas = listOf(programa.copy(usuarioInscrito = false))

        val mensajes = mensajesDurante(vm) { vm.cancelarInscripcion(programa.id) }

        assertEquals(listOf("No tienes una inscripción activa en este programa."), mensajes)
        assertEquals(2, catalogo.llamadas)
        assertEquals("Inscribirme", tarjetas(vm)[0].textoBoton)
    }

    @Test
    fun `cancelar - programa ya iniciado no llega al servidor`() {
        val programa = programaValido().copy(usuarioInscrito = true, fechaInicio = AHORA)
        val inscripciones = RepositorioFalso()
        val vm = viewModel(CatalogoFalso(listOf(programa)), inscripciones)
        // Ya inició: la tarjeta muestra Inscrito deshabilitado, no Cancelar
        assertEquals("Inscrito", tarjetas(vm)[0].textoBoton)
        assertFalse(tarjetas(vm)[0].botonHabilitado)

        val mensajes = mensajesDurante(vm) { vm.cancelarInscripcion(programa.id) }

        assertTrue(inscripciones.cancelaciones.isEmpty())
        assertEquals(listOf("Este programa ya comenzó, ya no es posible cancelar."), mensajes)
    }

    // ------------------------------------------------------------------------------
    // DE DÓNDE VIENEN LOS DATOS: ProgramaRepositoryImpl (mapper ProgramaDto -> Programa)
    // ------------------------------------------------------------------------------

    private val gson = Gson()

    private fun jsonPrograma(
        estado: String? = "\"Disponible\"",
        inicio: String = "2026-12-31T23:59:59",
        fin: String = "2027-01-01T00:00:00",
        // null = el backend no manda el campo (así se prueba su ausencia, no un null explícito)
        usuarioInscrito: Boolean? = null,
        cupoMaximo: String? = null,
        cupoDisponible: String? = null,
    ): String {
        val extras =
            listOfNotNull(
                usuarioInscrito?.let { "\"usuario_inscrito\":$it" },
                cupoMaximo?.let { "\"cupo_maximo\":$it" },
                cupoDisponible?.let { "\"cupo_disponible\":$it" },
            ).joinToString("") { ",$it" }
        return """{"id_programa":"p","tipo":"t","nombre":"n","estado_del_programa":$estado,
        "fecha_inicio":"$inicio","fecha_fin":"$fin"$extras}"""
    }

    private fun programasDesdeJson(json: String): List<Programa> {
        val respuesta = gson.fromJson("""{"success":true,"data":[$json]}""", ProgramasResponseDto::class.java)
        val api =
            object : ProgramaApiService {
                override suspend fun obtenerProgramas() = respuesta

                override suspend fun obtenerMisProgramas() = respuesta
            }
        return runBlocking { ProgramaRepositoryImpl(api).obtenerProgramasVigentes() }
    }

    @Test
    fun `limites - la fecha se interpreta en la zona del refugio`() {
        val programa = programasDesdeJson(jsonPrograma()).single()

        assertEquals(Instant.parse("2027-01-01T05:59:59Z"), programa.fechaInicio)
    }

    @Test
    fun `nulos - un estado null se convierte en no disponible`() {
        val programa = programasDesdeJson(jsonPrograma(estado = "null")).single()

        assertEquals(EstadoPrograma.NO_DISPONIBLE, programa.estado)
    }

    @Test
    fun `nulos - sin usuario_inscrito ni cupo se asume no inscrito y sin limite`() {
        // Si el backend no manda los campos, el mapper asume que el usuario NO está inscrito
        // (`== true` convierte el null en false) y que el programa no tiene límite (null).
        val programa = programasDesdeJson(jsonPrograma()).single()

        assertNull(programa.cupoMaximo)
        assertNull(programa.cupoDisponible)
        assertFalse(programa.usuarioInscrito)
    }

    @Test
    fun `nulos - cupo_maximo y cupo_disponible null explicitos quedan en null`() {
        val programa = programasDesdeJson(jsonPrograma(cupoMaximo = "null", cupoDisponible = "null")).single()

        assertNull(programa.cupoMaximo)
        assertNull(programa.cupoDisponible)
    }

    @Test
    fun `estructural - cupo_maximo y cupo_disponible llegan a la entidad`() {
        val programa = programasDesdeJson(jsonPrograma(cupoMaximo = "15", cupoDisponible = "7")).single()

        assertEquals(15, programa.cupoMaximo)
        assertEquals(7, programa.cupoDisponible)
    }

    @Test
    fun `limites - un cupo_disponible negativo se convierte en 0`() {
        for ((recibido, esperado) in listOf("-1" to 0, "-1000" to 0, "0" to 0, "1" to 1)) {
            val programa = programasDesdeJson(jsonPrograma(cupoMaximo = "10", cupoDisponible = recibido)).single()

            assertEquals("cupo_disponible=$recibido", esperado, programa.cupoDisponible)
        }
    }

    @Test
    fun `estructural - el mapper respeta usuario_inscrito del backend`() {
        val programa = programasDesdeJson(jsonPrograma(usuarioInscrito = true)).single()

        assertTrue(programa.usuarioInscrito)
    }

    @Test
    fun `limites - una fecha invalida omite solo ese programa`() {
        val valido = jsonPrograma(inicio = "2026-12-01T10:00:00")

        // +18:01 está justo después del límite: Java no acepta zonas mayores a ±18:00.
        for (fecha in listOf("", "2026-02-29T00:00:00", "2026-12-31T23:59:59+18:01", "2026-12-31T23:59:59-18:01")) {
            for (invalido in listOf(jsonPrograma(inicio = fecha), jsonPrograma(fin = fecha))) {
                val programas = programasDesdeJson("$invalido,$valido,$invalido")

                assertEquals(
                    "fecha='$fecha'",
                    listOf(Instant.parse("2026-12-01T16:00:00Z")),
                    programas.map { it.fechaInicio },
                )
            }
        }
    }

    @Test
    fun `limites - una fecha con o sin zona ya no omite el programa`() {
        val casos =
            listOf(
                "2026-12-31T23:59:59Z" to "2026-12-31T23:59:59Z",
                "2026-12-31T23:59:59+00:00" to "2026-12-31T23:59:59Z",
                "2026-12-31T17:59:59-06:00" to "2026-12-31T23:59:59Z",
                "2026-12-31T23:59:59+18:00" to "2026-12-31T05:59:59Z",
                "2026-12-31T23:59:59-18:00" to "2027-01-01T17:59:59Z",
                "2026-12-31T23:59:59" to "2027-01-01T05:59:59Z",
                "2026-12-31T23:59:59.629" to "2027-01-01T05:59:59.629Z",
            )

        for ((fecha, esperado) in casos) {
            val programa = programasDesdeJson(jsonPrograma(inicio = fecha, fin = fecha)).single()

            assertEquals("inicio='$fecha'", Instant.parse(esperado), programa.fechaInicio)
            assertEquals("fin='$fecha'", Instant.parse(esperado), programa.fechaFin)
        }
    }
}
