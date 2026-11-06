package com.app.android.solicitudes

import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.useCases.useCasesSolicitudes.GetCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarCompromisoUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarBorradorUseCase
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.CompromisoViewModel
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
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

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 1: COBERTURA DE VALORES VACÍOS Y BLANCOS (VOID VALUE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Los campos `petNeeds` (cuidados básicos) y `petSterilization` (opinión de esterilización)
     * son obligatorios y deben rechazar entradas vacías o solo con espacios.
     */
    @Test
    fun `void_value_coverage_empty_and_whitespace_answers_return_errors`() {
        prepararBorradorValido()

        // Caso A: Respuestas completamente vacías (Empty `""`)
        val datosVacios =
            Compromiso(
                petNeeds = "",
                petSterilization = "",
                petFollowing = true,
            )
        val erroresVacios = guardarCompromisoUseCase(datosVacios)
        assertNotNull("petNeeds vacío debe generar error", erroresVacios.petNeeds)
        assertNotNull("petSterilization vacío debe generar error", erroresVacios.petSterilization)
        assertEquals("Escribe tu respuesta", erroresVacios.petNeeds)
        assertEquals("Escribe tu respuesta", erroresVacios.petSterilization)

        // Caso B: Respuestas con solo espacios en blanco (`"   "`) que deben ser limpiadas con trim()
        val datosEspacios =
            Compromiso(
                petNeeds = "   ",
                petSterilization = "\t \n ",
                petFollowing = true,
            )
        val erroresEspacios = guardarCompromisoUseCase(datosEspacios)
        assertNotNull("petNeeds con solo espacios debe fallar", erroresEspacios.petNeeds)
        assertNotNull("petSterilization con solo espacios debe fallar", erroresEspacios.petSterilization)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 2: COBERTURA DE CONDICIONES Y DECISIONES (DECISION / CONDITION COVERAGE)
     * ---------------------------------------------------------------------------------
     * Evaluamos el campo booleano `petFollowing` (permiso para dar seguimiento).
     * Regla: No puede ser nulo (`null`).
     */
    @Test
    fun `decision_coverage_pet_following_null_returns_error`() {
        prepararBorradorValido()

        // Intentar guardar con petFollowing nulo
        val datosSinSeleccion =
            Compromiso(
                petNeeds = "Alimentación, agua fresca, vacunas y paseos diarios",
                petSterilization = "Es vital para su salud y control poblacional",
                petFollowing = null, // Nulo inválido
            )
        val errores = guardarCompromisoUseCase(datosSinSeleccion)
        assertNotNull("petFollowing no puede ser nulo", errores.petFollowing)
        assertEquals("Selecciona una opción", errores.petFollowing)
    }

    @Test
    fun `decision_coverage_pet_following_valid_booleans_pass`() {
        prepararBorradorValido()

        // Opción afirmativa (true)
        val datosSi =
            Compromiso(
                petNeeds = "Cuidados veterinarios y amor",
                petSterilization = "Muy importante",
                petFollowing = true,
            )
        val erroresSi = guardarCompromisoUseCase(datosSi)
        assertFalse("Con petFollowing = true no debe haber errores", erroresSi.hasErrors)

        // Opción negativa (false)
        val datosNo = datosSi.copy(petFollowing = false)
        val erroresNo = guardarCompromisoUseCase(datosNo)
        assertFalse("Con petFollowing = false no debe haber errores", erroresNo.hasErrors)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 3: PRUEBAS DE INTEGRACIÓN CON VIEWMODEL Y FLUJOS DE ESTADO
     * ---------------------------------------------------------------------------------
     * Validamos que el [CompromisoViewModel] cargue datos, actualice la UI y guarde correctamente.
     */
    @Test
    fun `viewmodel_load_and_save_success_flow`() {
        prepararBorradorValido()
        val viewModel = CompromisoViewModel(GetCompromisoUseCase(borradorRepository), guardarCompromisoUseCase)

        // 1. Cargar estado inicial
        viewModel.onLoadCompromiso()
        val initialState = viewModel.uiState.value
        assertTrue("El estado inicial tras cargar debe ser Success", initialState is CompromisoUiState.Success)

        // 2. Modificar con respuestas válidas completas
        val datosValidos =
            Compromiso(
                petNeeds = "Alimento de calidad, visitas al veterinario y ejercicio diario",
                petSterilization = "Es necesaria para evitar el abandono y cuidar su salud",
                petFollowing = true,
            )
        viewModel.onDatosChange(datosValidos)

        // 3. Ejecutar continuar (guardado exitoso)
        viewModel.onContinuar()
        assertTrue("El indicador 'listo' debe activarse al guardar correctamente", viewModel.listo.value)

        // 4. Verificar persistencia en el repositorio (Borrador)
        val guardado = getCompromisoUseCase()
        assertEquals("Alimento de calidad, visitas al veterinario y ejercicio diario", guardado.petNeeds)
        assertEquals(true, guardado.petFollowing)
    }

    @Test
    fun `viewmodel_save_with_errors_does_not_set_ready`() {
        prepararBorradorValido()
        val viewModel = CompromisoViewModel(GetCompromisoUseCase(borradorRepository), guardarCompromisoUseCase)
        viewModel.onLoadCompromiso()

        // Enviar datos incompletos (petNeeds vacío y petFollowing nulo)
        val datosInvalidos =
            Compromiso(
                petNeeds = "",
                petSterilization = "Buena idea",
                petFollowing = null,
            )
        viewModel.onDatosChange(datosInvalidos)
        viewModel.onContinuar()

        // Comprobar que NO se marca como listo y se registran los errores
        assertFalse("No debe marcarse como listo si existen errores de validación", viewModel.listo.value)

        val currentState = viewModel.uiState.value as CompromisoUiState.Success
        assertTrue("El estado de errores debe estar activo", currentState.errores.hasErrors)
        assertNotNull(currentState.errores.petNeeds)
        assertNotNull(currentState.errores.petFollowing)
    }
}
