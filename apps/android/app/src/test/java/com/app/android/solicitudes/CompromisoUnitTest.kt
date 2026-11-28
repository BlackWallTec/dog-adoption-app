package com.app.android.solicitudes

import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.GetCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GetEdicionUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarBorradorUseCase
import com.app.android.presentation.viewmodel.viewModelSolicitudes.Acceptances
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

// Testing asistidos por IA para comprobar los diferentes casos de testeo:
// Guiado por el prompt aportado por el profesor Luis Corral para unit testing
// stress testing - range coverage - void value coverage - boundary value analysis, - structural coverage

class CompromisoUnitTest {
    private val borradorRepository = BorradorRepositoryImpl()
    private val guardarCompromisoUseCase = GuardarCompromisoUseCase(borradorRepository)
    private val getCompromisoUseCase = GetCompromisoUseCase(borradorRepository)

    // Inicializador auxiliar para preparar un borrador válido antes de probar casos específicos
    private fun prepararBorradorValido() =
        runBlocking {
            val fakeRepo = SolicitudRepositoryFake()
            val iniciarBorrador = IniciarBorradorUseCase(fakeRepo, borradorRepository)
            iniciarBorrador("usuarioTest", "perroTest")
        }

    // Un compromiso completo por ejemplo:

    private fun datosValidos() =
        Compromiso(
            petNeeds = "Alimento de calidad, visitas al veterinario y ejercicio diario",
            petSterilization = "Es necesaria para evitar el abandono y cuidar su salud",
            hasVet = true,
            vetDetail = "Veterinaria Patitas, a dos cuadras de mi casa",
            timeAlone = "4 horas al día; mi mamá lo cuida si viajo",
            dailyRoutine = "Dos paseos, comida a sus horas y duerme en la sala",
            dogManagement = "Lo llevaría con un adiestrador y le daría más paseos",
        )

    // Los tres checkboxes al final de la view
    private val todoAceptado =
        Acceptances(acceptsReturn = true, petSpending = true, petFollowing = true)

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 1: COBERTURA DE VALORES VACÍOS Y BLANCOS (VOID VALUE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Todos los campos de texto son obligatorios y deben rechazar entradas vacías
     * o solo con espacios.
     */

    @Test
    fun `void_value_coverage_empty_and_whitespace_answers_return_errors`() {
        prepararBorradorValido()

        // Caso A: Respuestas completamente vacías (Empty `""`)
        val erroresVacios = guardarCompromisoUseCase(datosValidos().copy(petNeeds = "", petSterilization = ""))
        assertEquals("Escribe tu respuesta", erroresVacios.petNeeds)
        assertEquals("Escribe tu respuesta", erroresVacios.petSterilization)

        // Caso B: Respuestas con solo espacios en blanco (`"   "`) que deben ser limpiadas con trim()
        val erroresEspacios =
            guardarCompromisoUseCase(datosValidos().copy(petNeeds = "   ", petSterilization = "\t \n "))
        assertNotNull("petNeeds con solo espacios debe fallar", erroresEspacios.petNeeds)
        assertNotNull("petSterilization con solo espacios debe fallar", erroresEspacios.petSterilization)
    }

    @Test
    fun `void_value_coverage_every_text_field_is_required`() {
        prepararBorradorValido()

        val errores =
            guardarCompromisoUseCase(
                datosValidos().copy(vetDetail = "", timeAlone = " ", dailyRoutine = "", dogManagement = "  "),
            )

        assertTrue(errores.hasErrors)
        assertEquals("Escribe tu respuesta", errores.vetDetail)
        assertEquals("Escribe tu respuesta", errores.timeAlone)
        assertEquals("Escribe tu respuesta", errores.dailyRoutine)
        assertEquals("Escribe tu respuesta", errores.dogManagement)
        // Los campos que sí están bien no marcan error
        assertNull(errores.petNeeds)
        assertNull(errores.hasVet)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 2: COBERTURA DE CONDICIONES Y DECISIONES (DECISION / CONDITION COVERAGE)
     * ---------------------------------------------------------------------------------
     * Evaluamos el campo booleano `hasVet` (¿cuenta con veterinario de confianza?).
     * Regla: no puede ser nulo (`null`); tanto Sí como No son respuestas válidas.
     */
    @Test
    fun `decision_coverage_has_vet_null_returns_error`() {
        prepararBorradorValido()

        val errores = guardarCompromisoUseCase(datosValidos().copy(hasVet = null))

        assertTrue(errores.hasErrors)
        assertEquals("Selecciona una opción", errores.hasVet)
    }

    @Test
    fun `decision_coverage_has_vet_valid_booleans_pass`() {
        prepararBorradorValido()

        // Opción afirmativa (true)
        val erroresSi = guardarCompromisoUseCase(datosValidos().copy(hasVet = true))
        assertFalse("Con hasVet = true no debe haber errores", erroresSi.hasErrors)

        // Opción negativa (false)
        val erroresNo = guardarCompromisoUseCase(datosValidos().copy(hasVet = false))
        assertFalse("Con hasVet = false no debe haber errores", erroresNo.hasErrors)
        assertNull(erroresNo.hasVet)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 3: LOS CHECKBOXES (BOUNDARY VALUE ANALYSIS)
     * ---------------------------------------------------------------------------------
     * Los tres checkboxes son estado de la pantalla (Acceptances): solo se permite
     * continuar cuando los tres están marcados.
     */
    @Test
    fun `boundary_acceptances_all_accepted_only_when_the_three_are_checked`() {
        assertFalse("Sin marcar ninguno no está aceptado", Acceptances().allAccepted)
        assertFalse(todoAceptado.copy(acceptsReturn = false).allAccepted)
        assertFalse(todoAceptado.copy(petSpending = false).allAccepted)
        assertFalse(todoAceptado.copy(petFollowing = false).allAccepted)
        assertTrue(todoAceptado.allAccepted)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 4: PRUEBAS DE INTEGRACIÓN CON VIEWMODEL Y FLUJOS DE ESTADO
     * ---------------------------------------------------------------------------------
     * Validamos que el [CompromisoViewModel] cargue datos, actualice la UI y guarde correctamente.
     */
    @Test
    fun `viewmodel_load_and_save_success_flow`() {
        prepararBorradorValido()
        val viewModel =
            CompromisoViewModel(
                GetCompromisoUseCase(borradorRepository),
                guardarCompromisoUseCase,
                GetEdicionUseCase(borradorRepository),
            )
        // 1. Cargar estado inicial
        viewModel.onLoadCompromiso()
        val initialState = viewModel.uiState.value
        assertTrue("El estado inicial tras cargar debe ser Success", initialState is CompromisoUiState.Success)

        // 2. Modificar con respuestas válidas completas y marcar los tres checkboxes
        viewModel.onDatosChange(datosValidos())
        viewModel.onAcceptancesChange(todoAceptado)

        // 3. Ejecutar continuar (guardado exitoso)
        viewModel.onContinuar()
        assertTrue("El indicador 'listo' debe activarse al guardar correctamente", viewModel.listo.value)

        // 4. Verificar persistencia en el repositorio (Borrador)
        val guardado = getCompromisoUseCase()
        assertEquals(datosValidos().petNeeds, guardado.petNeeds)
        assertEquals(true, guardado.hasVet)
    }

    @Test
    fun `viewmodel_save_with_errors_does_not_set_ready`() {
        prepararBorradorValido()
        val viewModel =
            CompromisoViewModel(
                GetCompromisoUseCase(borradorRepository),
                guardarCompromisoUseCase,
                GetEdicionUseCase(borradorRepository),
            )
        viewModel.onLoadCompromiso()

        // Los checkboxes marcados, para que sí llegue a validar
        viewModel.onAcceptancesChange(todoAceptado)

        // Datos incompletos (petNeeds vacío y hasVet sin responder)
        viewModel.onDatosChange(datosValidos().copy(petNeeds = "", hasVet = null))
        viewModel.onContinuar()

        // Comprobar que NO se marca como listo y se registran los errores
        assertFalse("No debe marcarse como listo si existen errores de validación", viewModel.listo.value)

        val currentState = viewModel.uiState.value as CompromisoUiState.Success
        assertTrue("El estado de errores debe estar activo", currentState.errores.hasErrors)
        assertNotNull(currentState.errores.petNeeds)
        assertNotNull(currentState.errores.hasVet)
    }

    @Test
    fun `viewmodel_does_not_continue_until_all_checkboxes_are_accepted`() {
        prepararBorradorValido()
        val viewModel =
            CompromisoViewModel(
                GetCompromisoUseCase(borradorRepository),
                guardarCompromisoUseCase,
                GetEdicionUseCase(borradorRepository),
            )
        viewModel.onLoadCompromiso()
        viewModel.onDatosChange(datosValidos())

        // Falta un checkbox: el botón está desactivado y no avanza
        viewModel.onAcceptancesChange(todoAceptado.copy(petFollowing = false))
        viewModel.onContinuar()
        assertFalse("Sin los tres checkboxes no debe avanzar", viewModel.listo.value)

        // Con los tres marcados sí avanza
        viewModel.onAcceptancesChange(todoAceptado)
        viewModel.onContinuar()
        assertTrue("Con los tres checkboxes marcados debe avanzar", viewModel.listo.value)
    }
}
