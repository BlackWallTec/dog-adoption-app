package com.app.android.solicitudes

import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.useCases.useCasesSolicitudes.GetTuHogarUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarTuHogarUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarBorradorUseCase
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.TuHogarViewModel
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

class TuHogarUnitTest {
    private val borradorRepository = BorradorRepositoryImpl()
    private val guardarTuHogarUseCase = GuardarTuHogarUseCase(borradorRepository)
    private val getTuHogarUseCase = GetTuHogarUseCase(borradorRepository)

    // Inicializador auxiliar para preparar un borrador válido antes de probar casos específicos
    private fun prepararBorradorValido() =
        runBlocking {
            val fakeRepo = SolicitudRepositoryFake()
            val iniciarBorrador = IniciarBorradorUseCase(fakeRepo, borradorRepository)
            iniciarBorrador("usuarioTest", "perroTest")
        }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 1: ANÁLISIS DE VALORES DE FRONTERA Y COBERTURA DE RANGOS (RANGE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Probamos el campo `peopleInHouse` (cantidad de personas en el hogar).
     * Regla de negocio: Debe haber al menos 1 persona en el hogar (`peopleInHouse >= 1`).
     */
    @Test
    fun `boundary_people_in_house_less_than_one_returns_error`() {
        prepararBorradorValido()

        // Frontera inválida 1: Cero personas en casa
        val datosCero =
            TuHogar(
                whyAdopt = "Quiero un perrito",
                whereLive = "En la sala",
                hasGarden = "Patio grande",
                allAgree = true,
                peopleInHouse = 0,
            )
        val erroresCero = guardarTuHogarUseCase(datosCero)
        assertNotNull("Debe fallar si hay 0 personas", erroresCero.peopleInHouse)
        assertEquals("Debe de haber al menos 1 persona", erroresCero.peopleInHouse)

        // Frontera inválida 2: Número negativo de personas
        val datosNegativos = datosCero.copy(peopleInHouse = -3)
        val erroresNegativos = guardarTuHogarUseCase(datosNegativos)
        assertNotNull("Debe fallar con números negativos", erroresNegativos.peopleInHouse)
    }

    @Test
    fun `boundary_people_in_house_valid_ranges_passes`() {
        prepararBorradorValido()

        // Frontera válida exacta: 1 persona (mínimo permitido)
        val datosMinimo =
            TuHogar(
                whyAdopt = "Compañía",
                whereLive = "Dormitorio",
                hasGarden = "Sí",
                allAgree = true,
                peopleInHouse = 1,
            )
        val erroresMinimo = guardarTuHogarUseCase(datosMinimo)
        assertFalse("Con 1 persona no debe haber error de cantidad", erroresMinimo.hasErrors)

        // Rango superior normal: 5 personas y 10 personas
        val datosFamiliaGrande = datosMinimo.copy(peopleInHouse = 10)
        val erroresFamiliaGrande = guardarTuHogarUseCase(datosFamiliaGrande)
        assertFalse("Con 10 personas debe pasar perfectamente", erroresFamiliaGrande.hasErrors)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 2: COBERTURA DE VALORES VACÍOS Y BLANCOS (VOID VALUE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Verificamos que los textos obligatorios (`whyAdopt`, `whereLive`, `hasGarden`)
     * rechacen cadenas vacías `""` o cadenas llenas de espacios `"   "` (gracias al trim()).
     */
    @Test
    fun `void_value_coverage_blank_and_whitespace_texts_trigger_errors`() {
        prepararBorradorValido()

        // Caso A: Textos completamente vacíos (Empty)
        val datosVacios =
            TuHogar(
                whyAdopt = "",
                whereLive = "",
                hasGarden = "",
                allAgree = true,
                peopleInHouse = 2,
            )
        val erroresVacios = guardarTuHogarUseCase(datosVacios)
        assertNotNull("whyAdopt vacío debe dar error", erroresVacios.whyAdopt)
        assertNotNull("whereLive vacío debe dar error", erroresVacios.whereLive)
        assertNotNull("hasGarden vacío debe dar error", erroresVacios.hasGarden)

        // Caso B: Textos con solo espacios en blanco (Whitespace) que el sistema debe limpiar con trim()
        val datosEspacios =
            TuHogar(
                whyAdopt = "   ",
                whereLive = "      ",
                hasGarden = " \t ",
                allAgree = true,
                peopleInHouse = 2,
            )
        val erroresEspacios = guardarTuHogarUseCase(datosEspacios)
        assertNotNull("whyAdopt con solo espacios debe ser detectado como vacío", erroresEspacios.whyAdopt)
        assertNotNull("whereLive con solo espacios debe ser detectado como vacío", erroresEspacios.whereLive)
        assertNotNull("hasGarden con solo espacios debe ser detectado como vacío", erroresEspacios.hasGarden)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 3: COBERTURA DE CONDICIONES Y DECISIONES (DECISION / CONDITION COVERAGE)
     * ---------------------------------------------------------------------------------
     * Evaluamos la lógica condicional de mascotas:
     * `val hasPets = limpios.hadPet == true || limpios.otherPets == true`
     * Si `hasPets` es true, los campos `hadPet`, `otherPets` y `aboutPets` se vuelven obligatorios.
     */
    @Test
    fun `decision_coverage_pets_conditional_validation`() {
        prepararBorradorValido()

        // Subcaso 3.1: No tiene mascotas (hasPets = false). No se exigen detalles de mascotas.
        val datosSinMascotas =
            TuHogar(
                whyAdopt = "Amor animal",
                whereLive = "Patio y sala",
                hasGarden = "Sí",
                allAgree = true,
                peopleInHouse = 3,
                hadPet = false,
                otherPets = false,
            )
        val erroresSinMascotas = guardarTuHogarUseCase(datosSinMascotas)
        assertFalse("Sin mascotas no debe requerir explicacion de mascotas", erroresSinMascotas.hasErrors)

        // Subcaso 3.2: Tuvo mascota antes (hadPet = true) pero se dejó otherPets como null -> Error en otherPets
        val datosIncompletos1 = datosSinMascotas.copy(hadPet = true, otherPets = null)
        val errores1 = guardarTuHogarUseCase(datosIncompletos1)
        assertNotNull("Debe exigir seleccionar si tiene otros animales actuales", errores1.otherPets)

        // Subcaso 3.3: Tiene otros animales (otherPets = true), hadPet = true, pero aboutPets está vacío -> Error en aboutPets
        val datosIncompletos2 = datosSinMascotas.copy(hadPet = true, otherPets = true, aboutPets = "")
        val errores2 = guardarTuHogarUseCase(datosIncompletos2)
        assertNotNull("Debe exigir contar sobre las mascotas si tiene o tuvo", errores2.aboutPets)
        assertEquals("Cuéntanos sobre tus mascotas", errores2.aboutPets)

        // Subcaso 3.4: Todo completo correctamente con mascotas
        val datosMascotasCompletas =
            datosSinMascotas.copy(
                hadPet = true,
                otherPets = true,
                aboutPets = "Tengo un gato amigable esterilizado",
            )
        val erroresCompletos = guardarTuHogarUseCase(datosMascotasCompletas)
        assertFalse("Con datos de mascotas completos no debe haber errores", erroresCompletos.hasErrors)
    }

    @Test
    fun `decision_coverage_all_agree_validation`() {
        prepararBorradorValido()

        // El campo `allAgree` (¿todos están de acuerdo?) no puede ser nulo
        val datosSinAcuerdo =
            TuHogar(
                whyAdopt = "Compañía",
                whereLive = "Adentro",
                hasGarden = "Sí",
                peopleInHouse = 2,
                allAgree = null, // Nulo inválido
            )
        val errores = guardarTuHogarUseCase(datosSinAcuerdo)
        assertNotNull("allAgree no puede ser nulo", errores.allAgree)
        assertEquals("Selecciona una opción", errores.allAgree)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 4: PRUEBAS DE INTEGRACIÓN CON VIEWMODEL Y FLUJOS DE ESTADO
     * ---------------------------------------------------------------------------------
     * Validamos que el [TuHogarViewModel] gestione correctamente los estados de carga,
     * actualización de datos y éxito al guardar en el borrador.
     */
    @Test
    fun `viewmodel_load_and_save_success_flow`() {
        prepararBorradorValido()
        val viewModel = TuHogarViewModel(GetTuHogarUseCase(borradorRepository), guardarTuHogarUseCase)

        // 1. Cargar datos iniciales
        viewModel.onLoadTuHogar()
        val initialState = viewModel.uiState.value
        assertTrue("El estado inicial tras cargar debe ser Success", initialState is TuHogarUiState.Success)

        // 2. Modificar datos con información válida
        val datosValidos =
            TuHogar(
                whyAdopt = "Quiero rescatar un peludo",
                whereLive = "Dormirá en su camita en la sala",
                hasGarden = "Patio trasero amplio",
                allAgree = true,
                peopleInHouse = 3,
                hadPet = false,
                otherPets = false,
                houseType = HouseType.CASA,
            )
        viewModel.onDatosChange(datosValidos)

        // 3. Ejecutar continuar (guardado exitoso)
        viewModel.onContinuar()
        assertTrue("El indicador 'listo' debe activarse al guardar sin errores", viewModel.listo.value)

        // 4. Verificar que se guardó correctamente en el repositorio (Borrador)
        val guardadoEnBorrador = getTuHogarUseCase()
        assertEquals("Quiero rescatar un peludo", guardadoEnBorrador.whyAdopt)
        assertEquals(3, guardadoEnBorrador.peopleInHouse)
        assertEquals(HouseType.CASA, guardadoEnBorrador.houseType)
    }

    @Test
    fun `viewmodel_save_with_errors_does_not_set_ready`() {
        prepararBorradorValido()
        val viewModel = TuHogarViewModel(GetTuHogarUseCase(borradorRepository), guardarTuHogarUseCase)
        viewModel.onLoadTuHogar()

        // Intentar continuar con datos incompletos (whyAdopt vacío, allAgree nulo, peopleInHouse = 0)
        val datosInvalidos =
            TuHogar(
                whyAdopt = "",
                whereLive = "",
                hasGarden = "",
                allAgree = null,
                peopleInHouse = 0,
            )
        viewModel.onDatosChange(datosInvalidos)
        viewModel.onContinuar()

        // Comprobar que NO se marcó como listo y se mantienen los errores en el estado
        assertFalse("No debe marcarse como listo si hay errores de validación", viewModel.listo.value)

        val currentState = viewModel.uiState.value as TuHogarUiState.Success
        assertTrue("Debe tener errores activos", currentState.errores.hasErrors)
        assertNotNull(currentState.errores.whyAdopt)
        assertNotNull(currentState.errores.allAgree)
        assertNotNull(currentState.errores.peopleInHouse)
    }
}
