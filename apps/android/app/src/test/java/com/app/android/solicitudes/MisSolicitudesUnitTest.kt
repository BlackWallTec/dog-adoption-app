package com.app.android.solicitudes

import com.app.android.data.dtos.dtosSolicitudes.PerroSolicitudDto
import com.app.android.data.dtos.dtosSolicitudes.SolicitudClienteDto
import com.app.android.data.dtos.dtosSolicitudes.toDomain
import com.app.android.data.dtos.dtosSolicitudes.toEstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import com.app.android.domain.useCases.useCasesSolicitudes.GetMisSolicitudesUseCase
import com.app.android.presentation.util.utilSolicitudes.toItem
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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
import java.util.TimeZone
import kotlin.coroutines.cancellation.CancellationException

// Testing asistidos por IA para comprobar los diferentes casos de testeo:
// Guiado por el prompt aportado por el profesor Luis Corral para unit testing
// stress testing - range coverage - void value coverage - boundary value analysis, - structural coverage

@OptIn(ExperimentalCoroutinesApi::class)
class MisSolicitudesUnitTest {
    private class FakeSolicitudRepository : SolicitudRepositoryInt {
        var solicitudesARetornar: List<SolicitudCliente> = emptyList()
        var excepcionASimular: Exception? = null

        // NUEVO: si se le asigna una compuerta, el repositorio "se queda esperando" hasta que
        // el test la abra. Sirve para poder observar el estado Loading.
        var compuerta: CompletableDeferred<Unit>? = null

        override suspend fun getMisSolicitudes(): List<SolicitudCliente> {
            compuerta?.await()
            excepcionASimular?.let { throw it }
            return solicitudesARetornar
        }

        override suspend fun getSolicitudActiva(
            idUsuario: String,
            idPerro: String,
        ): SolicitudResumen? = null

        override suspend fun getPerfil(idUsuario: String): DatosGenerales = throw NotImplementedError()
    }

    private val repositoryFake = FakeSolicitudRepository()
    private val useCase = GetMisSolicitudesUseCase(repositoryFake)
    private lateinit var viewModel: MisSolicitudesViewModel

    // NUEVO: zona horaria original, para restaurarla al terminar cada test
    private lateinit var zonaOriginal: TimeZone

    // @Before corre antes de CADA test:
    //  - Fijamos la zona horaria en UTC: el formateador de fechas usa la zona del teléfono
    //    (ZoneId.systemDefault()), y sin esto un test podría pasar en tu compu y fallar en otra.
    //  - viewModelScope usa el hilo principal de Android, que no existe en un test: le ponemos uno de prueba.
    @Before
    fun setUp() {
        zonaOriginal = TimeZone.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = MisSolicitudesViewModel(useCase)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(zonaOriginal)
        Dispatchers.resetMain()
    }

    private fun crearSolicitudCliente(
        id: String = "sol-1",
        folio: String = "QA-00000001",
        fecha: String = "2026-10-12T18:30:00Z",
        estado: EstadoSolicitud = EstadoSolicitud.PENDIENTE_REVISION,
        nombrePerro: String? = "Bruno",
        fotoUrl: String? = "https://example.com/foto.jpg",
        documentoRechazado: Boolean = false,
        comentarioAdmin: String? = null,
    ): SolicitudCliente =
        SolicitudCliente(
            idSolicitud = id,
            folio = folio,
            fechaSolicitud = fecha,
            estado = estado,
            nombrePerro = nombrePerro,
            fotoUrl = fotoUrl,
            documentoRechazado = documentoRechazado,
            comentarioAdmin = comentarioAdmin,
        )

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 1 (NUEVO): CAPA DE DATOS, DEL JSON DEL SERVIDOR A LA ENTITY
     * ---------------------------------------------------------------------------------
     * El backend manda el estado como texto ("Aprobada Definitiva") y cualquier campo del JSON
     * puede venir null. Esta capa lo convierte en una entity segura, sin nulos donde la app
     * no los espera. Antes de esta sección, los tests arrancaban ya desde la entity.
     */
    @Test
    fun `range_coverage_texto_del_estado_se_convierte_en_enum`() {
        // Cada valor válido del rango (los 4 estados que existen en la BD)
        assertEquals(EstadoSolicitud.PENDIENTE_REVISION, "Pendiente de Revisión".toEstadoSolicitud())
        assertEquals(EstadoSolicitud.EN_PERIODO_PRUEBA, "En Periodo de Prueba".toEstadoSolicitud())
        assertEquals(EstadoSolicitud.APROBADA, "Aprobada Definitiva".toEstadoSolicitud())
        assertEquals(EstadoSolicitud.RECHAZADA, "Rechazada".toEstadoSolicitud())
    }

    @Test
    fun `void_value_coverage_estado_nulo_vacio_o_casi_correcto_es_desconocido`() {
        assertEquals(EstadoSolicitud.DESCONOCIDO, (null as String?).toEstadoSolicitud())
        assertEquals(EstadoSolicitud.DESCONOCIDO, "".toEstadoSolicitud())
        // La comparación es EXACTA: el backend ya limpia los espacios, la app no perdona diferencias
        assertEquals(EstadoSolicitud.DESCONOCIDO, "rechazada".toEstadoSolicitud()) // minúscula
        assertEquals(EstadoSolicitud.DESCONOCIDO, "Rechazada ".toEstadoSolicitud()) // espacio al final
        assertEquals(EstadoSolicitud.DESCONOCIDO, "Borrador".toEstadoSolicitud()) // el backend no lo manda
    }

    @Test
    fun `structural_coverage_dto_completo_se_mapea_a_la_entity`() {
        val entity =
            SolicitudClienteDto(
                idSolicitud = "sol-9",
                folio = "QA-00000009",
                fechaSolicitud = "2026-10-12T18:30:00Z",
                estado = "Aprobada Definitiva",
                perro = PerroSolicitudDto(nombre = "Bruno", fotoUrl = "https://example.com/bruno.jpg"),
                fotoUrl = null,
                documentoRechazado = true,
                comentarioAdmin = "Todo en orden",
            ).toDomain()

        assertEquals("sol-9", entity.idSolicitud)
        assertEquals("QA-00000009", entity.folio)
        assertEquals("2026-10-12T18:30:00Z", entity.fechaSolicitud)
        assertEquals(EstadoSolicitud.APROBADA, entity.estado)
        assertEquals("Bruno", entity.nombrePerro)
        assertEquals("https://example.com/bruno.jpg", entity.fotoUrl)
        assertTrue(entity.documentoRechazado)
        assertEquals("Todo en orden", entity.comentarioAdmin)
    }

    @Test
    fun `void_value_coverage_dto_con_todo_nulo_usa_valores_seguros`() {
        val entity =
            SolicitudClienteDto(
                idSolicitud = null,
                folio = null,
                fechaSolicitud = null,
                estado = null,
                perro = null,
                fotoUrl = null,
                documentoRechazado = null,
                comentarioAdmin = null,
            ).toDomain()

        // Los textos obligatorios quedan "" (no null), el estado DESCONOCIDO y el booleano false
        assertEquals("", entity.idSolicitud)
        assertEquals("", entity.folio)
        assertEquals("", entity.fechaSolicitud)
        assertEquals(EstadoSolicitud.DESCONOCIDO, entity.estado)
        assertFalse(entity.documentoRechazado)
        // Lo opcional sigue siendo null
        assertNull(entity.nombrePerro)
        assertNull(entity.fotoUrl)
        assertNull(entity.comentarioAdmin)
    }

    @Test
    fun `void_value_coverage_dto_con_textos_en_blanco_los_convierte_en_null`() {
        // Un texto vacío o con solo espacios es lo mismo que "no hay dato"
        val entity =
            SolicitudClienteDto(
                idSolicitud = "1",
                folio = "QA-00000001",
                fechaSolicitud = "2026-10-12T18:30:00Z",
                estado = "Rechazada",
                perro = PerroSolicitudDto(nombre = "   ", fotoUrl = ""),
                fotoUrl = null,
                documentoRechazado = false,
                comentarioAdmin = "   ",
            ).toDomain()

        assertNull(entity.nombrePerro)
        assertNull(entity.fotoUrl)
        assertNull(entity.comentarioAdmin)
        assertEquals(EstadoSolicitud.RECHAZADA, entity.estado)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 2: COBERTURA DE VALORES VACÍOS Y NULOS (VOID VALUE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Evalúa el comportamiento del mapeador cuando los campos vienen nulos, vacíos o
     * con formato de fecha inválido desde la base de datos.
     */
    @Test
    fun `void_value_coverage_nombre_perro_nulo_se_transforma_en_mascota_no_disponible`() {
        val solicitudConPerroNulo = crearSolicitudCliente(nombrePerro = null)
        val item = solicitudConPerroNulo.toItem()
        assertEquals("Mascota no disponible", item.nombrePerro)
    }

    @Test
    fun `void_value_coverage_foto_y_comentario_nulos_se_preservan_como_null`() {
        val solicitudSinFotoNiComentario = crearSolicitudCliente(fotoUrl = null, comentarioAdmin = null)
        val item = solicitudSinFotoNiComentario.toItem()
        assertNull(item.fotoUrl)
        assertNull(item.comentarioAdmin)
    }

    @Test
    fun `void_value_coverage_fecha_invalida_o_vacia_retorna_cadena_vacia`() {
        val itemVacio = crearSolicitudCliente(fecha = "").toItem()
        val itemCorrupto = crearSolicitudCliente(fecha = "fecha-no-valida-123").toItem()
        assertEquals("", itemVacio.fecha)
        assertEquals("", itemCorrupto.fecha)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 3: ANÁLISIS DE VALORES LÍMITE (BOUNDARY VALUE ANALYSIS)
     * ---------------------------------------------------------------------------------
     * Comprueba los límites de tamaño en listas (0 y 1 elemento), fechas en bordes
     * de año/mes (bisiestos, 1 ene, 31 dic) y la conversión de los 12 meses.
     */
    @Test
    fun `boundary_value_analysis_lista_vacia_de_solicitudes`() {
        repositoryFake.solicitudesARetornar = emptyList()
        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value
        assertTrue(state is MisSolicitudesUiState.Success)
        val successState = state as MisSolicitudesUiState.Success
        assertTrue(successState.solicitudes.isEmpty())
    }

    @Test
    fun `boundary_value_analysis_lista_con_un_solo_elemento`() {
        repositoryFake.solicitudesARetornar = listOf(crearSolicitudCliente(id = "sol-unica"))
        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value as MisSolicitudesUiState.Success
        assertEquals(1, state.solicitudes.size)
        assertEquals("sol-unica", state.solicitudes.first().idSolicitud)
    }

    @Test
    fun `boundary_value_analysis_fechas_limite_inicio_fin_de_ano_y_bisiesto`() {
        val inicioAno = crearSolicitudCliente(fecha = "2026-01-01T12:00:00Z").toItem()
        assertEquals("1 ene 2026", inicioAno.fecha)

        val finAno = crearSolicitudCliente(fecha = "2026-12-31T12:00:00Z").toItem()
        assertEquals("31 dic 2026", finAno.fecha)

        val bisiesto = crearSolicitudCliente(fecha = "2024-02-29T12:00:00Z").toItem()
        assertEquals("29 feb 2024", bisiesto.fecha)

        val sinZonaHoraria = crearSolicitudCliente(fecha = "2026-05-15").toItem()
        assertEquals("15 may 2026", sinZonaHoraria.fecha)
    }

    // NUEVO: fechas que NO existen en el calendario
    @Test
    fun `boundary_value_analysis_fechas_imposibles_retornan_cadena_vacia`() {
        // 2026 no es bisiesto: el 29 de febrero no existe
        assertEquals("", crearSolicitudCliente(fecha = "2026-02-29T12:00:00Z").toItem().fecha)
        // Mes y día imposibles
        assertEquals("", crearSolicitudCliente(fecha = "2026-13-45").toItem().fecha)
    }

    // NUEVO: el formato REAL con el que Supabase manda las fechas (milisegundos y +00:00)
    @Test
    fun `range_coverage_fecha_en_formato_real_de_supabase`() {
        val item = crearSolicitudCliente(fecha = "2026-10-06T22:07:36.789+00:00").toItem()
        assertEquals("6 oct 2026", item.fecha) // el día no lleva cero a la izquierda
    }

    // NUEVO: la fecha se muestra en la zona horaria del teléfono
    @Test
    fun `boundary_value_analysis_zona_horaria_puede_cambiar_el_dia`() {
        // 02:00 UTC del día 13 todavía es el día 12 por la noche en México (UTC-6)
        val iso = "2026-10-13T02:00:00Z"

        assertEquals("13 oct 2026", crearSolicitudCliente(fecha = iso).toItem().fecha) // UTC (fijada en setUp)

        TimeZone.setDefault(TimeZone.getTimeZone("America/Mexico_City")) // tearDown la restaura
        assertEquals("12 oct 2026", crearSolicitudCliente(fecha = iso).toItem().fecha)
    }

    @Test
    fun `boundary_value_analysis_cobertura_de_los_12_meses_del_ano`() {
        val fechasPrueba =
            listOf(
                "2026-01-10T12:00:00Z" to "10 ene 2026",
                "2026-02-10T12:00:00Z" to "10 feb 2026",
                "2026-03-10T12:00:00Z" to "10 mar 2026",
                "2026-04-10T12:00:00Z" to "10 abr 2026",
                "2026-05-10T12:00:00Z" to "10 may 2026",
                "2026-06-10T12:00:00Z" to "10 jun 2026",
                "2026-07-10T12:00:00Z" to "10 jul 2026",
                "2026-08-10T12:00:00Z" to "10 ago 2026",
                "2026-09-10T12:00:00Z" to "10 sep 2026",
                "2026-10-10T12:00:00Z" to "10 oct 2026",
                "2026-11-10T12:00:00Z" to "10 nov 2026",
                "2026-12-10T12:00:00Z" to "10 dic 2026",
            )

        fechasPrueba.forEach { (iso, esperado) ->
            val item = crearSolicitudCliente(fecha = iso).toItem()
            assertEquals(esperado, item.fecha)
        }
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 4: COBERTURA ESTRUCTURAL Y DE DECISIONES (DECISION COVERAGE)
     * ---------------------------------------------------------------------------------
     * Evalúa la matriz de decisión completa de la regla de negocio `puedeEditar`:
     * requiere documentoRechazado = true Y estado == PENDIENTE_REVISION.
     */
    @Test
    fun `decision_condition_coverage_tabla_de_verdad_regla_puede_editar`() {
        val casoTrueTrue =
            crearSolicitudCliente(
                documentoRechazado = true,
                estado = EstadoSolicitud.PENDIENTE_REVISION,
            )
        assertTrue(casoTrueTrue.puedeEditar)

        val casoTrueAprobada =
            crearSolicitudCliente(
                documentoRechazado = true,
                estado = EstadoSolicitud.APROBADA,
            )
        assertFalse(casoTrueAprobada.puedeEditar)

        val casoTrueRechazada =
            crearSolicitudCliente(
                documentoRechazado = true,
                estado = EstadoSolicitud.RECHAZADA,
            )
        assertFalse(casoTrueRechazada.puedeEditar)

        val casoTruePrueba =
            crearSolicitudCliente(
                documentoRechazado = true,
                estado = EstadoSolicitud.EN_PERIODO_PRUEBA,
            )
        assertFalse(casoTruePrueba.puedeEditar)

        val casoTrueDesconocido =
            crearSolicitudCliente(
                documentoRechazado = true,
                estado = EstadoSolicitud.DESCONOCIDO,
            )
        assertFalse(casoTrueDesconocido.puedeEditar)

        val casoFalsePendiente =
            crearSolicitudCliente(
                documentoRechazado = false,
                estado = EstadoSolicitud.PENDIENTE_REVISION,
            )
        assertFalse(casoFalsePendiente.puedeEditar)

        val casoFalseRechazada =
            crearSolicitudCliente(
                documentoRechazado = false,
                estado = EstadoSolicitud.RECHAZADA,
            )
        assertFalse(casoFalseRechazada.puedeEditar)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 5: PRUEBAS DE ESTRÉS Y RANGOS (STRESS TESTING & RANGE COVERAGE)
     * ---------------------------------------------------------------------------------
     * Verifica la estabilidad y rendimiento del ViewModel al procesar conjuntos masivos
     * (1,000 elementos) y listas con datos altamente heterogéneos.
     */
    @Test
    fun `stress_testing_carga_masiva_de_1000_solicitudes`() {
        val solicitudesMasivas =
            (1..1000).map { index ->
                crearSolicitudCliente(
                    id = "sol-$index",
                    folio = "QA-%08d".format(index),
                    estado = if (index % 2 == 0) EstadoSolicitud.PENDIENTE_REVISION else EstadoSolicitud.APROBADA,
                    documentoRechazado = (index % 5 == 0),
                )
            }
        repositoryFake.solicitudesARetornar = solicitudesMasivas

        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value as MisSolicitudesUiState.Success
        assertEquals(1000, state.solicitudes.size)
        assertEquals("sol-1", state.solicitudes.first().idSolicitud)
        assertEquals("sol-1000", state.solicitudes.last().idSolicitud)
        // NUEVO: la regla también se cumple a escala. Pueden editar los pares (en revisión)
        // que además son múltiplos de 5, o sea los múltiplos de 10: 100 solicitudes.
        assertEquals(100, state.solicitudes.count { it.puedeEditar })
    }

    @Test
    fun `range_coverage_conjunto_de_datos_heterogeneo_y_mixto`() {
        val datosMixtos =
            listOf(
                crearSolicitudCliente(id = "1", nombrePerro = "Rocky", fecha = "2026-10-12T18:30:00Z"),
                crearSolicitudCliente(id = "2", nombrePerro = null, fotoUrl = null, fecha = ""),
                crearSolicitudCliente(
                    id = "3",
                    nombrePerro = "Toby",
                    documentoRechazado = true,
                    estado = EstadoSolicitud.PENDIENTE_REVISION,
                ),
            )
        repositoryFake.solicitudesARetornar = datosMixtos

        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value as MisSolicitudesUiState.Success
        val items = state.solicitudes

        assertEquals(3, items.size)
        assertEquals("Rocky", items[0].nombrePerro)
        assertEquals("Mascota no disponible", items[1].nombrePerro)
        assertTrue(items[2].puedeEditar)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 6: INTEGRACIÓN Y MÁQUINA DE ESTADOS DE VIEWMODEL (STATE FLOW COVERAGE)
     * ---------------------------------------------------------------------------------
     * Valida el ciclo de vida del StateFlow en el ViewModel: estado inicial Loading,
     * transiciones a Success, Error y recuperación en reintentos.
     */
    @Test
    fun `viewmodel_estado_inicial_es_loading`() {
        val nuevoViewModel = MisSolicitudesViewModel(useCase)
        assertEquals(MisSolicitudesUiState.Loading, nuevoViewModel.uiState.value)
    }

    // NUEVO: se observa el Loading en transición, no solo el estado inicial
    @Test
    fun `viewmodel_muestra_loading_mientras_espera_la_respuesta_del_servidor`() {
        val compuerta = CompletableDeferred<Unit>()
        repositoryFake.compuerta = compuerta
        repositoryFake.solicitudesARetornar = listOf(crearSolicitudCliente())

        viewModel.onLoadSolicitudes()
        assertEquals(MisSolicitudesUiState.Loading, viewModel.uiState.value) // sigue esperando

        compuerta.complete(Unit) // el servidor "responde"
        assertTrue(viewModel.uiState.value is MisSolicitudesUiState.Success)
    }

    @Test
    fun `viewmodel_flujo_de_error_ante_fallo_de_conexion`() {
        repositoryFake.excepcionASimular = RuntimeException("Fallo de conexión 500")

        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value
        assertTrue(state is MisSolicitudesUiState.Error)
        val errorState = state as MisSolicitudesUiState.Error
        assertEquals(
            "No se pudieron cargar tus solicitudes. Revisa tu conexión e inténtalo de nuevo.",
            errorState.mensaje,
        )
    }

    @Test
    fun `viewmodel_reintento_exitoso_tras_un_error`() {
        repositoryFake.excepcionASimular = RuntimeException("Timeout")
        viewModel.onLoadSolicitudes()
        assertTrue(viewModel.uiState.value is MisSolicitudesUiState.Error)

        repositoryFake.excepcionASimular = null
        repositoryFake.solicitudesARetornar = listOf(crearSolicitudCliente(id = "sol-recuperada"))

        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value as MisSolicitudesUiState.Success
        assertEquals(1, state.solicitudes.size)
        assertEquals("sol-recuperada", state.solicitudes.first().idSolicitud)
    }

    @Test
    fun `viewmodel_cancellation_exception_no_se_captura_como_error_de_ui`() {
        repositoryFake.excepcionASimular = CancellationException("Corrutina cancelada por el sistema")

        viewModel.onLoadSolicitudes()

        assertFalse(viewModel.uiState.value is MisSolicitudesUiState.Error)
    }

    /**
     * ---------------------------------------------------------------------------------
     * SECCIÓN 7: CASOS FALLIDOS Y EXCEPCIONALES (FAILED & UNHAPPY PATH TEST CASES)
     * ---------------------------------------------------------------------------------
     * Prueba escenarios donde las operaciones fallan, el servidor responde con datos
     * corruptos o errores no controlados para verificar la resiliencia de la app.
     */
    @Test
    fun `testeo_fallido_servidor_lanza_excepcion_generica_no_deja_ui_en_loading`() {
        repositoryFake.excepcionASimular = IllegalStateException("Error interno del servidor")

        viewModel.onLoadSolicitudes()

        val state = viewModel.uiState.value
        assertFalse("El estado NO debe quedarse en Loading ante un error inesperado", state is MisSolicitudesUiState.Loading)
        assertTrue("El estado debe ser Error ante cualquier excepción no controlada", state is MisSolicitudesUiState.Error)
    }

    @Test
    fun `testeo_fallido_errores_consecutivos_mantienen_estado_de_error_consistente`() {
        // Fallo 1
        repositoryFake.excepcionASimular = RuntimeException("Error 1")
        viewModel.onLoadSolicitudes()
        val error1 = (viewModel.uiState.value as MisSolicitudesUiState.Error).mensaje

        // Fallo 2 consecutivo
        repositoryFake.excepcionASimular = RuntimeException("Error 2")
        viewModel.onLoadSolicitudes()
        val error2 = (viewModel.uiState.value as MisSolicitudesUiState.Error).mensaje

        assertEquals("El mensaje de error debe ser consistente entre fallas", error1, error2)
    }
}
