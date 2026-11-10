package com.app.android.voluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramasResponseDto
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
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel
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
 * Comunica sus resultados de dos formas:
 *   - `uiState`: lo que la pantalla dibuja (cargando, vacía, lista o error).
 *   - `mensajes`: los textos del snackbar, que se muestran una sola vez.
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
        override suspend fun obtenerProgramasVigentes(): List<Programa> {
            compuerta?.await()
            falla?.let { throw it }
            return programas
        }
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

    private fun programasEnPantalla(vm: ProgramasViewModel): List<Programa> =
        (vm.uiState.value as ProgramasUiState.Success).programas

    // ------------------------------------------------------------------------------
    // CARGA DE LA LISTA
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - con programas muestra la lista`() {
        val vm = viewModel(CatalogoFalso(listOf(programaValido())))

        assertEquals(ProgramasUiState.Success(listOf(programaValido())), vm.uiState.value)
    }

    @Test
    fun `vacios - sin programas muestra el estado vacio`() {
        val vm = viewModel(CatalogoFalso(emptyList()))

        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `limites - un solo programa ya no se considera vacio`() {
        val vm = viewModel(CatalogoFalso(listOf(programaValido())))

        assertEquals(1, programasEnPantalla(vm).size)
    }

    @Test
    fun `rangos - carga listas de distintos tamanos`() {
        for (tamano in listOf(1, 2, 10, 1000)) {
            val programas = List(tamano) { programaValido().copy(id = "programa-$it") }

            val vm = viewModel(CatalogoFalso(programas))

            assertEquals("tamano=$tamano", tamano, programasEnPantalla(vm).size)
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

        // El usuario toca "Reintentar" cuando el servidor ya volvió.
        catalogo.falla = null
        vm.cargarProgramas()

        assertTrue(vm.uiState.value is ProgramasUiState.Success)
    }

    // ------------------------------------------------------------------------------
    // INSCRIBIRSE: ESTRUCTURAL
    // ------------------------------------------------------------------------------

    @Test
    fun `estructural - al inscribirse marca solo ese programa y avisa`() {
        val otro = programaValido().copy(id = "programa-2")
        val vm = viewModel(CatalogoFalso(listOf(programaValido(), otro)))

        val mensajes = mensajesDurante(vm) { vm.inscribirse(programaValido()) }

        assertEquals(listOf("¡Te inscribiste en Paseo de perritos!"), mensajes)
        // Es la decisión `if (it.id == idPrograma)` en sus dos caminos:
        assertTrue(programasEnPantalla(vm)[0].usuarioInscrito)
        assertFalse(programasEnPantalla(vm)[1].usuarioInscrito)
    }

    @Test
    fun `estructural - si el servidor dice YaInscrito tambien marca la tarjeta`() {
        val respuesta = Resultado.Error(VoluntariadoError.YaInscrito)
        val vm = viewModel(inscripciones = RepositorioFalso(respuesta = respuesta))

        val mensajes = mensajesDurante(vm) { vm.inscribirse(programaValido()) }

        assertEquals(listOf("Ya estás inscrito en este programa."), mensajes)
        assertTrue(programasEnPantalla(vm)[0].usuarioInscrito)
    }

    @Test
    fun `estructural - cada error muestra su mensaje y solo YaInscrito marca la tarjeta`() {
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

            val mensajes = mensajesDurante(vm) { vm.inscribirse(programaValido()) }

            assertEquals("error=$error", listOf(mensaje), mensajes)
            assertEquals(
                "error=$error",
                error == VoluntariadoError.YaInscrito,
                programasEnPantalla(vm)[0].usuarioInscrito,
            )
        }
    }

    @Test
    fun `estructural - muchos toques mientras se envia solo generan una peticion`() {
        val compuerta = CompletableDeferred<Unit>()
        val inscripciones = RepositorioFalso(compuerta = compuerta)
        val vm = viewModel(inscripciones = inscripciones)

        val mensajes =
            mensajesDurante(vm) {
                vm.inscribirse(programaValido())

                // Estrés: mientras la petición "viaja", 100 toques más no deben hacer nada.
                repeat(100) { vm.inscribirse(programaValido()) }
                vm.inscribirse(programaValido().copy(id = "programa-2"))

                compuerta.complete(Unit)
            }

        assertEquals(listOf("programa-1"), inscripciones.llamadas.toList())
        assertEquals(1, mensajes.size)
    }

    @Test
    fun `estructural - al terminar un envio se puede volver a intentar`() {
        val respuesta = Resultado.Error(VoluntariadoError.SinConexion)
        val inscripciones = RepositorioFalso(respuesta = respuesta)
        val vm = viewModel(inscripciones = inscripciones)

        val mensajes =
            mensajesDurante(vm) {
                vm.inscribirse(programaValido())
                vm.inscribirse(programaValido())
            }

        assertEquals(2, inscripciones.llamadas.size)
        assertEquals(2, mensajes.size)
    }

    @Test
    fun `estructural - una cancelacion libera el boton gracias al finally`() {
        // Sin el bloque `finally`, `inscribiendo` se quedaría en true y el botón dejaría de
        // funcionar para siempre.
        var intentos = 0
        val inscripciones =
            object : InscripcionRepository {
                override suspend fun inscribirse(idPrograma: String): Resultado<Inscripcion> {
                    intentos++
                    if (intentos == 1) throw CancellationException("cancelada")
                    return Resultado.Exito(INSCRIPCION_OK)
                }
            }
        val vm = viewModel(inscripciones = inscripciones)

        val mensajes =
            mensajesDurante(vm) {
                vm.inscribirse(programaValido())
                vm.inscribirse(programaValido())
            }

        assertEquals(2, intentos)
        assertEquals(listOf("¡Te inscribiste en Paseo de perritos!"), mensajes)
    }

    @Test
    fun `estructural - si la pantalla no muestra una lista no se marca nada`() {
        // Es la decisión `if (actual is ProgramasUiState.Success)` en su camino falso.
        val vm = viewModel(CatalogoFalso(emptyList()))

        val mensajes = mensajesDurante(vm) { vm.inscribirse(programaValido()) }

        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
        assertEquals(1, mensajes.size) // aun así, el usuario recibe el aviso
    }

    // ------------------------------------------------------------------------------
    // INSCRIBIRSE: LÍMITES, RANGOS Y NULOS
    // ------------------------------------------------------------------------------

    @Test
    fun `limites - cupo y fecha en la frontera`() {
        val casos =
            listOf(
                programaValido().copy(cupoDisponible = 0) to false,
                programaValido().copy(cupoDisponible = 1) to true,
                programaValido().copy(fechaInicio = AHORA) to false,
                programaValido().copy(fechaInicio = AHORA.plusNanos(1)) to true,
            )

        for ((programa, llegaAlServidor) in casos) {
            val inscripciones = RepositorioFalso()
            val vm = viewModel(CatalogoFalso(listOf(programa)), inscripciones)

            mensajesDurante(vm) { vm.inscribirse(programa) }

            assertEquals("$programa", llegaAlServidor, inscripciones.llamadas.isNotEmpty())
            assertEquals("$programa", llegaAlServidor, programasEnPantalla(vm)[0].usuarioInscrito)
        }
    }

    @Test
    fun `rangos - cien programas distintos solo marcan el elegido`() {
        val programas = List(100) { programaValido().copy(id = "programa-$it") }
        val vm = viewModel(CatalogoFalso(programas))

        mensajesDurante(vm) { vm.inscribirse(programas[42]) }

        val marcados = programasEnPantalla(vm).filter { it.usuarioInscrito }.map { it.id }
        assertEquals(listOf("programa-42"), marcados)
    }

    @Test
    fun `nulos - con cupo sin limite tambien se puede inscribir`() {
        val programa = programaValido().copy(cupoMaximo = null, cupoDisponible = null)
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        mensajesDurante(vm) { vm.inscribirse(programa) }

        assertTrue(programasEnPantalla(vm)[0].usuarioInscrito)
        assertNull(programasEnPantalla(vm)[0].cupoDisponible)
    }

    @Test
    fun `vacios - un nombre vacio deja el mensaje incompleto`() {
        val programa = programaValido().copy(nombre = "")
        val vm = viewModel(CatalogoFalso(listOf(programa)))

        val mensajes = mensajesDurante(vm) { vm.inscribirse(programa) }

        assertEquals(listOf("¡Te inscribiste en !"), mensajes)
    }

    // ------------------------------------------------------------------------------
    // DE DÓNDE VIENEN LOS DATOS: ProgramaRepositoryImpl
    // ------------------------------------------------------------------------------

    private val gson = Gson()

    private fun jsonPrograma(
        estado: String? = "\"Disponible\"",
        inicio: String = "2026-12-31T23:59:59",
        fin: String = "2027-01-01T00:00:00",
    ) = """{"id_programa":"p","tipo":"t","nombre":"n","estado_del_programa":$estado,
        "fecha_inicio":"$inicio","fecha_fin":"$fin"}"""

    private fun programasDesdeJson(json: String): List<Programa> {
        val respuesta = gson.fromJson("""{"success":true,"data":[$json]}""", ProgramasResponseDto::class.java)
        val api =
            object : ProgramaApiService {
                override suspend fun obtenerProgramas() = respuesta
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
    fun `nulos - DOCUMENTA que la lista no trae cupo ni usuario inscrito`() {
        // El endpoint de la lista no manda estos datos. Por eso, en la app real, las reglas
        // YaInscrito y SinCupo nunca bloquean desde Android: solo el servidor las detecta.
        // Pendiente: historia de usuario de manejo de cupos.
        val programa = programasDesdeJson(jsonPrograma()).single()

        assertNull(programa.cupoDisponible)
        assertFalse(programa.usuarioInscrito)
    }

    @Test
    fun `limites - una fecha invalida omite solo ese programa`() {
        // ¿Qué bug había? Un solo programa con fecha mal formada lanzaba una excepción, y
        // ProgramasViewModel mostraba el error de carga en lugar de TODA la lista.
        // ¿Qué valida ahora? Que ese programa se omita y los demás sigan apareciendo, sin
        // importar si la fecha mala es la de inicio o la de fin. El de en medio (válido) usa
        // otra fecha para confirmar que sobrevive el programa correcto.
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
        // ¿Qué bug había? La lista y la inscripción leían las fechas de forma distinta: en la
        // lista, una fecha con "Z" contaba como inválida y el programa desaparecía.
        // ¿Qué valida ahora? Que ambas usen el mismo `aInstant()`. `single()` falla si el
        // programa se omite, y cada caso se prueba en `fecha_inicio` y en `fecha_fin`.
        val casos =
            listOf(
                // Con zona: la fecha ya dice cuál es, así que se respeta tal cual.
                "2026-12-31T23:59:59Z" to "2026-12-31T23:59:59Z",
                "2026-12-31T23:59:59+00:00" to "2026-12-31T23:59:59Z",
                "2026-12-31T17:59:59-06:00" to "2026-12-31T23:59:59Z",
                // Las zonas más extremas que Java acepta: justo en el límite.
                "2026-12-31T23:59:59+18:00" to "2026-12-31T05:59:59Z",
                "2026-12-31T23:59:59-18:00" to "2027-01-01T17:59:59Z",
                // Sin zona: se interpreta en la del refugio, 6 horas detrás de UTC.
                "2026-12-31T23:59:59" to "2027-01-01T05:59:59Z",
                // Así llegan hoy desde la base de datos, con milisegundos.
                "2026-12-31T23:59:59.629" to "2027-01-01T05:59:59.629Z",
            )

        for ((fecha, esperado) in casos) {
            val programa = programasDesdeJson(jsonPrograma(inicio = fecha, fin = fecha)).single()

            assertEquals("inicio='$fecha'", Instant.parse(esperado), programa.fechaInicio)
            assertEquals("fin='$fecha'", Instant.parse(esperado), programa.fechaFin)
        }
    }
}
