package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import com.app.android.domain.useCases.useCasesVoluntariado.CancelarInscripcionUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.FiltrarProgramasUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.InscribirseProgramaUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerMisProgramasUseCase
import com.app.android.domain.useCases.useCasesVoluntariado.ObtenerProgramasVigentesUseCase
import com.app.android.presentation.viewmodel.viewModelVoluntariado.AccionPrograma
import com.app.android.presentation.viewmodel.viewModelVoluntariado.MisProgramasViewModel
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramasViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Duration

/*
 * HU37 STRESS TESTS - VIEWMODELS (what the screen shows)
 *
 * ProgramasViewModelTest already covers the main cancel flow of the catalog screen: success,
 * double tap, NoInscrito reload and "already started". So here we add only what is missing:
 *   A. Catalog (ProgramasViewModel): the message for EACH of the 9 errors, void values.
 *   B. My Programs (MisProgramasViewModel): it has no tests yet, so its whole cancel flow is here.
 *
 * A ViewModel holds the screen state (uiState) plus one-time events (mensajes = snackbar texts).
 * Tests act like a user: tap the button, then check what the screen would show.
 *
 * Why Dispatchers.setMain? ViewModels run on the Android main thread, which does not exist in a
 * plain JVM test. `setMain(UnconfinedTestDispatcher())` swaps it for one that runs everything
 * immediately, so tests need no waiting. We undo it in @After so tests cannot affect each other.
 *
 * How do we simulate a slow network? RepositorioFalso has a "gate" (CompletableDeferred): while
 * closed, the server call hangs, so we can poke the screen mid-request, then open the gate.
 *
 * Why collect messages manually? A SharedFlow with no listener throws events away, so each test
 * first starts a listener (`Escucha`) that stores every message in a list.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CancelarViewModelsStressTest {
    @Before
    fun prepararHiloPrincipal() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @After
    fun restaurarHiloPrincipal() {
        Dispatchers.resetMain()
    }

    // ---- fakes and helpers ----------------------------------------------------------

    /*
     * A fake program catalog. `lista` is what the "server" currently has; tests can change it
     * between calls to simulate the server state changing. `cargas` counts how many times
     * the app asked for the list (so we can prove "no unnecessary reloads").
     */
    private class CatalogoFalso(var lista: List<Programa>, var fallar: Boolean = false) : ProgramaRepository {
        var cargas = 0

        override suspend fun obtenerProgramasVigentes(): List<Programa> = cargar()

        override suspend fun obtenerMisProgramas(): List<Programa> = cargar()

        private fun cargar(): List<Programa> {
            cargas++
            if (fallar) throw RuntimeException("sin servidor")
            return lista
        }
    }

    // A listener that stores everything a SharedFlow emits. Call `.cancel()` at the end.
    private class Escucha<T>(flujo: SharedFlow<T>, scope: CoroutineScope) {
        val eventos = mutableListOf<T>()
        val job: Job = scope.launch(Dispatchers.Unconfined) { flujo.collect { eventos.add(it) } }
    }

    private fun inscrito(id: String, nombre: String = "Programa $id", cupoDisponible: Int? = 5) =
        programaInscrito(id = id, nombre = nombre, cupoDisponible = cupoDisponible, cupoMaximo = 10)

    private fun inicioEn(programa: Programa, desdeAhora: Duration) = programa.copy(fechaInicio = AHORA.plus(desdeAhora))

    private fun crearCatalogo(
        repositorioInscripcion: RepositorioFalso,
        catalogo: CatalogoFalso,
    ): ProgramasViewModel =
        ProgramasViewModel(
            ObtenerProgramasVigentesUseCase(catalogo),
            InscribirseProgramaUseCase(repositorioInscripcion, RELOJ_FIJO),
            CancelarInscripcionUseCase(repositorioInscripcion, RELOJ_FIJO),
            RELOJ_FIJO,
            FiltrarProgramasUseCase(RELOJ_FIJO),
        )

    private fun crearMisProgramas(
        repositorioInscripcion: RepositorioFalso,
        catalogo: CatalogoFalso,
    ): MisProgramasViewModel =
        MisProgramasViewModel(
            ObtenerMisProgramasUseCase(catalogo),
            CancelarInscripcionUseCase(repositorioInscripcion, RELOJ_FIJO),
            RELOJ_FIJO,
        )

    private fun tarjetas(estado: ProgramasUiState): List<ProgramaUi> = (estado as ProgramasUiState.Success).programas

    private fun respuesta(error: VoluntariadoError): Resultado<Inscripcion> = Resultado.Error(error)

    // ==========================================================================
    // A. CATALOG SCREEN (ProgramasViewModel)
    // ==========================================================================

    @Test
    fun `rangos A - cada error posible muestra su mensaje y NO avisa de exito`() = runBlocking<Unit> {
        val esperados =
            mapOf(
                VoluntariadoError.SinCupo to "Este programa ya no tiene lugares disponibles.",
                VoluntariadoError.YaInscrito to "Ya estás inscrito en este programa.",
                VoluntariadoError.NoInscrito to "No tienes una inscripción activa en este programa.",
                // The one special case: the text says "cancelar", not "inscribirse".
                VoluntariadoError.ProgramaIniciado to "Este programa ya comenzó, ya no es posible cancelar.",
                VoluntariadoError.ProgramaNoDisponible to "Este programa no está disponible por ahora.",
                VoluntariadoError.ProgramaNoEncontrado to "No encontramos este programa. Actualiza la lista.",
                VoluntariadoError.NoAutenticado to "Inicia sesión para inscribirte.",
                VoluntariadoError.SinConexion to "Sin conexión. Revisa tu internet e inténtalo de nuevo.",
                VoluntariadoError.Desconocido to "Ocurrió un error. Inténtalo de nuevo.",
            )
        assertEquals("tabla completa: 9 errores", ERRORES_DE_VOLUNTARIADO.size, esperados.size)

        for (error in ERRORES_DE_VOLUNTARIADO) {
            val repo = RepositorioFalso(respuestaCancelacion = respuesta(error))
            val vm = crearCatalogo(repo, CatalogoFalso(listOf(inscrito("a"))))
            val mensajes = Escucha(vm.mensajes, this)
            val exitos = Escucha(vm.cancelacionExitosa, this)

            vm.cancelarInscripcion("a")

            assertEquals("$error (mensaje)", listOf(esperados.getValue(error)), mensajes.eventos)
            assertEquals("$error (sin evento de exito)", 0, exitos.eventos.size)
            mensajes.job.cancel()
            exitos.job.cancel()
        }
    }

    @Test
    fun `estructural A - un error normal no cambia la tarjeta ni recarga la lista`() = runBlocking<Unit> {
        val repo = RepositorioFalso(respuestaCancelacion = respuesta(VoluntariadoError.SinConexion))
        val catalogo = CatalogoFalso(listOf(inscrito("a")))
        val vm = crearCatalogo(repo, catalogo)

        vm.cancelarInscripcion("a")

        val tarjeta = tarjetas(vm.uiState.value).single()
        assertEquals("Cancelar", tarjeta.textoBoton) // still enrolled, can retry
        assertTrue(tarjeta.botonHabilitado) // the button is released again (finally block)
        assertEquals(1, catalogo.cargas)
    }

    @Test
    fun `vacio A - si la recarga tras NoInscrito falla, se conserva la lista y se avisa igual`() = runBlocking<Unit> {
        val repo = RepositorioFalso(respuestaCancelacion = respuesta(VoluntariadoError.NoInscrito))
        val catalogo = CatalogoFalso(listOf(inscrito("a")))
        val vm = crearCatalogo(repo, catalogo)
        val mensajes = Escucha(vm.mensajes, this)
        catalogo.fallar = true

        vm.cancelarInscripcion("a")

        assertEquals("Cancelar", tarjetas(vm.uiState.value).single().textoBoton)
        assertEquals(listOf("No tienes una inscripción activa en este programa."), mensajes.eventos)
        mensajes.job.cancel()
    }

    @Test
    fun `vacio A - ids que no existen no hacen nada (ni servidor, ni mensaje, ni cambio)`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val catalogo = CatalogoFalso(listOf(inscrito("a")))
        val vm = crearCatalogo(repo, catalogo)
        val mensajes = Escucha(vm.mensajes, this)
        val antes = vm.uiState.value

        for (id in listOf("", " ", "A", "a ", "zzz", "null", "a".repeat(10_000))) {
            vm.cancelarInscripcion(id)
        }

        assertTrue(repo.cancelaciones.isEmpty())
        assertTrue(mensajes.eventos.isEmpty())
        assertEquals(antes, vm.uiState.value)
        mensajes.job.cancel()
    }

    @Test
    fun `vacio A - sin programas (lista vacia) cancelar no truena`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val vm = crearCatalogo(repo, CatalogoFalso(emptyList()))

        vm.cancelarInscripcion("a")

        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
        assertTrue(repo.cancelaciones.isEmpty())
    }

    @Test
    fun `vacio A - cupos null (sin limite) tambien se cancelan sin error`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val sinLimite = programaInscrito(id = "a", cupoDisponible = null, cupoMaximo = null)
        val vm = crearCatalogo(repo, CatalogoFalso(listOf(sinLimite)))

        vm.cancelarInscripcion("a")

        assertEquals("Inscribirme", tarjetas(vm.uiState.value).single().textoBoton)
        assertEquals(listOf("a"), repo.cancelaciones.toList())
    }

    // ==========================================================================
    // B. MY PROGRAMS SCREEN (MisProgramasViewModel)
    // ==========================================================================

    @Test
    fun `estructural B - exito - la tarjeta sale de la lista y se avisa`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val vm = crearMisProgramas(repo, CatalogoFalso(listOf(inscrito("a", "Paseo"), inscrito("b"))))
        val mensajes = Escucha(vm.mensajes, this)

        vm.cancelarInscripcion("a")

        assertEquals(listOf("b"), tarjetas(vm.uiState.value).map { it.id })
        assertEquals(listOf("Cancelaste tu inscripción en Paseo."), mensajes.eventos)
        mensajes.job.cancel()
    }

    @Test
    fun `limites B - al cancelar la ultima tarjeta la pantalla queda Vacia`() = runBlocking<Unit> {
        val vm = crearMisProgramas(RepositorioFalso(), CatalogoFalso(listOf(inscrito("a"))))

        vm.cancelarInscripcion("a")

        assertEquals(ProgramasUiState.Empty, vm.uiState.value)
    }

    @Test
    fun `rangos B - cada error muestra su mensaje (la tabla propia de Mis Programas)`() = runBlocking<Unit> {
        val generico = "Ocurrió un error. Inténtalo de nuevo."
        val esperados =
            mapOf(
                VoluntariadoError.SinCupo to generico,
                VoluntariadoError.YaInscrito to generico,
                VoluntariadoError.NoInscrito to "No tienes una inscripción activa en este programa.",
                VoluntariadoError.ProgramaIniciado to "Este programa ya comenzó, ya no es posible cancelar.",
                VoluntariadoError.ProgramaNoDisponible to generico,
                VoluntariadoError.ProgramaNoEncontrado to "No encontramos este programa. Actualiza la lista.",
                VoluntariadoError.NoAutenticado to "Inicia sesión para continuar.",
                VoluntariadoError.SinConexion to "Sin conexión. Revisa tu internet e inténtalo de nuevo.",
                VoluntariadoError.Desconocido to generico,
            )
        assertEquals(ERRORES_DE_VOLUNTARIADO.size, esperados.size)

        for (error in ERRORES_DE_VOLUNTARIADO) {
            val vm = crearMisProgramas(RepositorioFalso(respuestaCancelacion = respuesta(error)), CatalogoFalso(listOf(inscrito("a"), inscrito("b"))))
            val mensajes = Escucha(vm.mensajes, this)

            vm.cancelarInscripcion("a")

            assertEquals("$error", listOf(esperados.getValue(error)), mensajes.eventos)
            // Only NoInscrito removes the card on error (the user is not enrolled anymore).
            val idsEsperados = if (error == VoluntariadoError.NoInscrito) listOf("b") else listOf("a", "b")
            assertEquals("$error (tarjetas)", idsEsperados, tarjetas(vm.uiState.value).map { it.id })
            mensajes.job.cancel()
        }
    }

    @Test
    fun `vacio B - ids inexistentes o vacios no hacen nada`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val vm = crearMisProgramas(repo, CatalogoFalso(listOf(inscrito("a"))))
        val mensajes = Escucha(vm.mensajes, this)

        for (id in listOf("", " ", "A", "zzz")) vm.cancelarInscripcion(id)

        assertTrue(repo.cancelaciones.isEmpty())
        assertTrue(mensajes.eventos.isEmpty())
        mensajes.job.cancel()
    }

    @Test
    fun `vacio B - lista vacia y error de carga`() = runBlocking<Unit> {
        val vacia = crearMisProgramas(RepositorioFalso(), CatalogoFalso(emptyList()))
        val rota = crearMisProgramas(RepositorioFalso(), CatalogoFalso(listOf(inscrito("a")), fallar = true))

        assertEquals(ProgramasUiState.Empty, vacia.uiState.value)
        assertEquals(ProgramasUiState.Error("No se pudieron cargar tus voluntariados"), rota.uiState.value)
        rota.cancelarInscripcion("a") // must not crash nor call the server
    }

    @Test
    fun `limites B - un programa que ya inicio sigue en la lista pero no se puede cancelar`() = runBlocking<Unit> {
        val repo = RepositorioFalso()
        val iniciado = inicioEn(inscrito("a"), Duration.ZERO) // starts exactly now
        val vm = crearMisProgramas(repo, CatalogoFalso(listOf(iniciado)))
        val mensajes = Escucha(vm.mensajes, this)

        val tarjeta = tarjetas(vm.uiState.value).single()
        assertEquals("el boton no ofrece cancelar", AccionPrograma.INSCRIBIRSE, tarjeta.accion)
        assertTrue(!tarjeta.botonHabilitado)

        vm.cancelarInscripcion("a") // even if somebody forces the call

        assertTrue(repo.cancelaciones.isEmpty())
        assertEquals(listOf("Este programa ya comenzó, ya no es posible cancelar."), mensajes.eventos)
        assertEquals("la tarjeta se queda", 1, tarjetas(vm.uiState.value).size)
        mensajes.job.cancel()
    }

    @Test
    fun `estructural B - doble toque - una sola peticion y el boton dice Cancelando`() = runBlocking<Unit> {
        val compuerta = CompletableDeferred<Unit>()
        val repo = RepositorioFalso(compuerta = compuerta)
        val vm = crearMisProgramas(repo, CatalogoFalso(listOf(inscrito("a"), inscrito("b"))))

        vm.cancelarInscripcion("a")
        vm.cancelarInscripcion("a")
        vm.cancelarInscripcion("b")

        val enCurso = tarjetas(vm.uiState.value).first { it.id == "a" }
        assertEquals("Cancelando…", enCurso.textoBoton)
        assertTrue(!enCurso.botonHabilitado)
        assertEquals(listOf("a"), repo.cancelaciones.toList())

        compuerta.complete(Unit)

        assertEquals(listOf("b"), tarjetas(vm.uiState.value).map { it.id })
    }

    @Test
    fun `estructural B - despues de un error el boton se libera y se puede reintentar`() = runBlocking<Unit> {
        val repo = RepositorioFalso(respuestaCancelacion = respuesta(VoluntariadoError.SinConexion))
        val vm = crearMisProgramas(repo, CatalogoFalso(listOf(inscrito("a"))))

        vm.cancelarInscripcion("a")
        vm.cancelarInscripcion("a")

        assertEquals(2, repo.cancelaciones.size)
        assertEquals("Cancelar", tarjetas(vm.uiState.value).single().textoBoton)
    }

}