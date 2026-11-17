package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.FiltrosProgramas
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.ResultadoFiltrado
import com.app.android.domain.useCases.useCasesVoluntariado.FiltrarProgramasUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

/**
 * Suite de pruebas unitarias exhaustiva para Filtrar Programas (HU32), .
 *
 * Estrategia de Pruebas (Stress Testing Strategy):
 * 1. Cobertura de Valores Nulos / Vacíos (Void Value Coverage)
 * 2. Análisis de Valores Límite (Boundary Value Analysis - BVA)
 * 3. Cobertura de Rangos (Range Coverage)
 * 4. Cobertura Estructural (Decision / Condition Coverage)
 *
 * NOTA DIDÁCTICA PARA PRINCIPIANTES:
 * Cada prueba explica mediante comentarios en español:
 * - El concepto que se está probando.
 * - Qué datos se envían a la función.
 * - Qué resultado se espera y por qué la regla de negocio lo dicta así.
 */
class FiltrarProgramasUseCaseTest {

    // Zona horaria de referencia del refugio ("America/Mexico_City").
    // Es fundamental fijar la zona horaria para garantizar que los test den el mismo resultado
    // independientemente del país o huso horario de la máquina donde corra la prueba.
    private val zonaRefugio: ZoneId = ZoneId.of("America/Mexico_City")

    // Fijamos un instante determinista en el tiempo para la prueba: 15 de Mayo de 2025 a las 12:00 UTC (06:00 a.m. en CDMX).
    private val ahoraFijo: Instant = Instant.parse("2025-05-15T12:00:00Z")
    
    // Fecha local equivalente en la zona del refugio para "ahoraFijo" -> 2025-05-15
    private val hoy: LocalDate = ahoraFijo.atZone(zonaRefugio).toLocalDate()

    private lateinit var clock: Clock
    private lateinit var useCase: FiltrarProgramasUseCase

    @Before
    fun setUp() {
        // Inyectamos un reloj con tiempo congelado en "ahoraFijo".
        clock = Clock.fixed(ahoraFijo, zonaRefugio)
        useCase = FiltrarProgramasUseCase(clock)
    }

    // ============================================================================================
    // MÉTODOS AUXILIARES PARA CONSTRUCCIÓN DE DATOS DE PRUEBA
    // ============================================================================================

    /**
     * Convierte una fecha ISO (yyyy-MM-dd) y hora opcional en la zona horaria del refugio a un Instant UTC.
     */
    private fun crearInstant(fechaIso: String, horaIso: String = "10:00:00"): Instant {
        val ldt = LocalDateTime.parse("${fechaIso}T${horaIso}")
        return ldt.atZone(zonaRefugio).toInstant()
    }

    /**
     * Crea un objeto [Programa] de prueba con valores por defecto válidos.
     */
    private fun crearPrograma(
        id: String = "prog_1",
        nombre: String = "Programa de Prueba",
        tipo: String = "Paseo",
        descripcion: String? = "Descripción de prueba",
        fechaInicio: Instant = crearInstant("2025-05-16", "10:00:00"),
        fechaFin: Instant = crearInstant("2025-05-16", "12:00:00"),
        ubicacion: String? = "Refugio Central",
        cupoMaximo: Int? = 10,
        cupoDisponible: Int? = 5,
        estado: EstadoPrograma = EstadoPrograma.DISPONIBLE,
        usuarioInscrito: Boolean = false,
    ): Programa {
        return Programa(
            id = id,
            nombre = nombre,
            tipo = tipo,
            descripcion = descripcion,
            fechaInicio = fechaInicio,
            fechaFin = fechaFin,
            ubicacion = ubicacion,
            cupoMaximo = cupoMaximo,
            cupoDisponible = cupoDisponible,
            estado = estado,
            usuarioInscrito = usuarioInscrito,
        )
    }

    // ============================================================================================
    // 1. COBERTURA DE VALORES NULOS Y VACÍOS (Void / Null Value Coverage)
    // ============================================================================================

    @Test
    fun `dado filtros vacios (todos nulos), devuelve todos los programas disponibles y futuros`() {
        // EXPLICACIÓN:
        // En Kotlin, un atributo nulo en `FiltrosProgramas` significa "sin filtro aplicado".
        // Si todos los filtros son `null`, solo se aplican las reglas base del catálogo:
        // programas que estén DISPONIBLES y cuya fecha de inicio sea posterior al instante actual.

        val programaValido = crearPrograma(id = "p1", fechaInicio = crearInstant("2025-05-16"))
        val programaPasado = crearPrograma(id = "p2", fechaInicio = crearInstant("2025-05-10"))
        val programaNoDisponible = crearPrograma(id = "p3", estado = EstadoPrograma.NO_DISPONIBLE)

        val resultado = useCase(listOf(programaValido, programaPasado, programaNoDisponible), FiltrosProgramas())

        assertTrue("El resultado debe ser Exito", resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals("Debe incluir únicamente el programa futuro disponible", 1, exito.programas.size)
        assertEquals("p1", exito.programas[0].id)
    }

    @Test
    fun `dada una lista de programas vacia, devuelve resultado exitoso con lista vacia`() {
        // EXPLICACIÓN:
        // Evaluamos el caso límite donde la base de datos o el API no retornan ningún programa.
        // El caso de uso debe responder con ResultadoFiltrado.Exito conteniendo una lista vacía.

        val resultado = useCase(emptyList(), FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertTrue(exito.programas.isEmpty())
    }

    @Test
    fun `dado un programa con atributos opcionales nulos, se procesa sin errores`() {
        // EXPLICACIÓN:
        // Los campos `descripcion`, `ubicacion`, `cupoMaximo` y `cupoDisponible` son opcionales (Nullable).
        // Verificamos que procesar programas con estos campos en `null` no genere NullPointerException.

        val programaConNulos = crearPrograma(
            id = "p_nulos",
            descripcion = null,
            ubicacion = null,
            cupoMaximo = null,
            cupoDisponible = null
        )

        val resultado = useCase(listOf(programaConNulos), FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p_nulos", exito.programas[0].id)
    }

    @Test
    fun `dado filtro solo con fechaDesde y fechaHasta nula, filtra solo por la fecha inicial`() {
        // EXPLICACIÓN DE NULO PARCIAL:
        // Si fechaHasta es null, se evalúa únicamente que la fecha del programa no sea anterior a fechaDesde.

        val p1Antes = crearPrograma(id = "p1", fechaInicio = crearInstant("2025-05-18"), fechaFin = crearInstant("2025-05-19"))
        val p2Despues = crearPrograma(id = "p2", fechaInicio = crearInstant("2025-05-20"), fechaFin = crearInstant("2025-05-21"))

        val filtros = FiltrosProgramas(fechaDesde = LocalDate.of(2025, 5, 20), fechaHasta = null)

        val resultado = useCase(listOf(p1Antes, p2Despues), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p2", exito.programas[0].id)
    }

    @Test
    fun `dado filtro solo con fechaHasta y fechaDesde nula, filtra solo por la fecha limite`() {
        // EXPLICACIÓN DE NULO PARCIAL:
        // Si fechaDesde es null, se evalúa únicamente que la fecha de inicio no sobrepase fechaHasta.

        val p1Dentro = crearPrograma(id = "p1", fechaInicio = crearInstant("2025-05-16"), fechaFin = crearInstant("2025-05-17"))
        val p2Fuera = crearPrograma(id = "p2", fechaInicio = crearInstant("2025-05-19"), fechaFin = crearInstant("2025-05-20"))

        val filtros = FiltrosProgramas(fechaDesde = null, fechaHasta = LocalDate.of(2025, 5, 18))

        val resultado = useCase(listOf(p1Dentro, p2Fuera), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p1", exito.programas[0].id)
    }

    // ============================================================================================
    // 2. ANÁLISIS DE VALORES LÍMITE (Boundary Value Analysis - BVA)
    // ============================================================================================

    @Test
    fun `dado fechaDesde igual a hoy, la validacion la acepta como fecha valida`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // Hoy es 2025-05-15.
        // Regla: `fechaDesde.isBefore(hoy)`.
        // Si fechaDesde == 2025-05-15 (hoy), `isBefore(hoy)` retorna FALSE, por lo que ES VÁLIDA.

        val filtros = FiltrosProgramas(fechaDesde = hoy)
        val programa = crearPrograma(fechaInicio = crearInstant("2025-05-16"))

        val resultado = useCase(listOf(programa), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
    }

    @Test
    fun `dado fechaDesde igual a ayer (un dia antes de hoy), la validacion la rechaza`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // Hoy es 2025-05-15.
        // Si fechaDesde == 2025-05-14 (ayer), `isBefore(hoy)` retorna TRUE, por lo que ES INVÁLIDA.

        val ayer = hoy.minusDays(1)
        val filtros = FiltrosProgramas(fechaDesde = ayer)

        val resultado = useCase(emptyList(), filtros)

        assertTrue(resultado is ResultadoFiltrado.Invalido)
        val invalido = resultado as ResultadoFiltrado.Invalido
        assertEquals("La fecha desde no puede ser anterior a hoy", invalido.errores.fechaDesde)
    }

    @Test
    fun `dado fechaHasta igual a fechaDesde (rango de un solo dia), es un rango valido`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // Si fechaDesde = 2025-05-20 y fechaHasta = 2025-05-20, `fechaHasta.isBefore(fechaDesde)` es FALSE.
        // El filtro de un solo día es un límite VÁLIDO.

        val fechaExacta = LocalDate.of(2025, 5, 20)
        val filtros = FiltrosProgramas(fechaDesde = fechaExacta, fechaHasta = fechaExacta)

        val programaMismoDia = crearPrograma(
            fechaInicio = crearInstant("2025-05-20", "09:00:00"),
            fechaFin = crearInstant("2025-05-20", "11:00:00")
        )

        val resultado = useCase(listOf(programaMismoDia), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
    }

    @Test
    fun `dado fechaHasta un dia menor que fechaDesde, la validacion la rechaza con error de fechaHasta`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // fechaDesde = 2025-05-20, fechaHasta = 2025-05-19.
        // `fechaHasta.isBefore(fechaDesde)` es TRUE -> INVÁLIDO.

        val fechaDesde = LocalDate.of(2025, 5, 20)
        val fechaHastaInvalida = LocalDate.of(2025, 5, 19)

        val filtros = FiltrosProgramas(fechaDesde = fechaDesde, fechaHasta = fechaHastaInvalida)

        val resultado = useCase(emptyList(), filtros)

        assertTrue(resultado is ResultadoFiltrado.Invalido)
        val invalido = resultado as ResultadoFiltrado.Invalido
        assertEquals("La fecha desde no puede ser posterior a la fecha hasta", invalido.errores.fechaHasta)
    }

    @Test
    fun `dado horaDesde igual a horaHasta (horas identicas), la validacion la rechaza`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // horaDesde = 10:00, horaHasta = 10:00.
        // Regla: `!horaDesde.isBefore(horaHasta)`.
        // Como 10:00 no es estrictamente menor a 10:00, la expresión se evalúa como TRUE y genera error.

        val hora = LocalTime.of(10, 0)
        val filtros = FiltrosProgramas(horaDesde = hora, horaHasta = hora)

        val resultado = useCase(emptyList(), filtros)

        assertTrue(resultado is ResultadoFiltrado.Invalido)
        val invalido = resultado as ResultadoFiltrado.Invalido
        assertEquals("La hora final debe ser posterior a la inicial", invalido.errores.horaHasta)
    }

    @Test
    fun `dado horaHasta un minuto posterior a horaDesde, es un rango horaria valido`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // horaDesde = 10:00, horaHasta = 10:01.
        // `horaDesde.isBefore(horaHasta)` es TRUE -> Rango VÁLIDO.

        val horaDesde = LocalTime.of(10, 0)
        val horaHastaValida = LocalTime.of(10, 1)

        val filtros = FiltrosProgramas(
            fechaDesde = hoy,
            horaDesde = horaDesde,
            horaHasta = horaHastaValida
        )

        val programa = crearPrograma(
            fechaInicio = crearInstant("2025-05-16", "10:00:00"),
            fechaFin = crearInstant("2025-05-16", "10:01:00")
        )

        val resultado = useCase(listOf(programa), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
    }

    @Test
    fun `dado programa cuya fechaInicio es exactamente igual a ahora, se excluye`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // Regla base: `programa.fechaInicio.isAfter(ahora)`.
        // Si fechaInicio == ahoraFijo, `isAfter` retorna FALSE. El programa debe ser excluido.

        val programaExacto = crearPrograma(fechaInicio = ahoraFijo)

        val resultado = useCase(listOf(programaExacto), FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertTrue(exito.programas.isEmpty())
    }

    @Test
    fun `dado programa cuya fechaInicio es un milisegundo posterior a ahora, se incluye`() {
        // ANÁLISIS DE VALOR LÍMITE (BVA):
        // Si fechaInicio == ahoraFijo + 1 ms, `isAfter` retorna TRUE. El programa debe ser incluido.

        val unMiliDespues = ahoraFijo.plusMillis(1)
        val programaFuturoInmediato = crearPrograma(id = "p_futuro", fechaInicio = unMiliDespues)

        val resultado = useCase(listOf(programaFuturoInmediato), FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p_futuro", exito.programas[0].id)
    }

    @Test
    fun `dado programa cuyo fin coincide con la fechaDesde del filtro, se incluye`() {
        // ANÁLISIS DE VALOR LÍMITE EN FILTRADO DE FECHAS:
        // Filtro fechaDesde = 2025-05-20.
        // Programa termina el 2025-05-20.
        // Regla: `cumpleDesde = (desde == null || !fin.isBefore(desde))`.
        // 2025-05-20 no es anterior a 2025-05-20, así que `!fin.isBefore(desde)` es TRUE -> INCLUIDO.

        val desde = LocalDate.of(2025, 5, 20)
        val filtros = FiltrosProgramas(fechaDesde = desde)

        val programaCoincideFin = crearPrograma(
            id = "p_fin_limite",
            fechaInicio = crearInstant("2025-05-19", "08:00:00"),
            fechaFin = crearInstant("2025-05-20", "18:00:00")
        )

        val resultado = useCase(listOf(programaCoincideFin), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
    }

    @Test
    fun `dado programa cuyo fin es un dia previo a fechaDesde, se excluye`() {
        // ANÁLISIS DE VALOR LÍMITE EN FILTRADO DE FECHAS:
        // Filtro fechaDesde = 2025-05-20.
        // Programa termina el 2025-05-19.
        // `fin.isBefore(desde)` es TRUE, por lo que `!fin.isBefore(desde)` es FALSE -> EXCLUIDO.

        val desde = LocalDate.of(2025, 5, 20)
        val filtros = FiltrosProgramas(fechaDesde = desde)

        val programaFinAntes = crearPrograma(
            id = "p_fin_antes",
            fechaInicio = crearInstant("2025-05-18", "08:00:00"),
            fechaFin = crearInstant("2025-05-19", "18:00:00")
        )

        val resultado = useCase(listOf(programaFinAntes), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertTrue(exito.programas.isEmpty())
    }

    // ============================================================================================
    // 3. COBERTURA DE RANGOS (Range Coverage)
    // ============================================================================================

    @Test
    fun `dado un rango de fechas, clasifica programas antes, dentro y despues del rango`() {
        // COBERTURA DE RANGOS:
        // Rango del filtro: 2025-05-20 a 2025-05-25.
        // Evaluamos 3 programas que cubren todo el espectro del rango:
        // 1) Antes del rango (fin en 2025-05-19) -> Excluido
        // 2) Dentro del rango (inicio 2025-05-21, fin 2025-05-22) -> Incluido
        // 3) Después del rango (inicio en 2025-05-26) -> Excluido

        val filtros = FiltrosProgramas(
            fechaDesde = LocalDate.of(2025, 5, 20),
            fechaHasta = LocalDate.of(2025, 5, 25)
        )

        val pAntes = crearPrograma(id = "antes", fechaInicio = crearInstant("2025-05-18"), fechaFin = crearInstant("2025-05-19"))
        val pDentro = crearPrograma(id = "dentro", fechaInicio = crearInstant("2025-05-21"), fechaFin = crearInstant("2025-05-22"))
        val pDespues = crearPrograma(id = "despues", fechaInicio = crearInstant("2025-05-26"), fechaFin = crearInstant("2025-05-27"))

        val resultado = useCase(listOf(pAntes, pDentro, pDespues), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("dentro", exito.programas[0].id)
    }

    @Test
    fun `dado un rango de horas (09-00 a 14-00), filtra los programas por franja horaria`() {
        // COBERTURA DE RANGOS DE HORAS:
        // Filtro: 09:00 a 14:00.
        // Regla:
        // cumpleDesde = (desde == null || fin.isAfter(desde))
        // cumpleHasta = (hasta == null || inicio.isBefore(hasta))
        //
        // 1) pTemprano (06:00 a 08:30): fin (08:30) no es posterior a desde (09:00) -> Excluido.
        // 2) pCorrecto (10:00 a 12:00): fin (12:00) > 09:00 e inicio (10:00) < 14:00 -> Incluido.
        // 3) pTarde (15:00 a 18:00): inicio (15:00) no es anterior a hasta (14:00) -> Excluido.

        val filtros = FiltrosProgramas(
            horaDesde = LocalTime.of(9, 0),
            horaHasta = LocalTime.of(14, 0)
        )

        val pTemprano = crearPrograma(id = "p_temprano", fechaInicio = crearInstant("2025-05-20", "06:00:00"), fechaFin = crearInstant("2025-05-20", "08:30:00"))
        val pCorrecto = crearPrograma(id = "p_correcto", fechaInicio = crearInstant("2025-05-20", "10:00:00"), fechaFin = crearInstant("2025-05-20", "12:00:00"))
        val pTarde = crearPrograma(id = "p_tarde", fechaInicio = crearInstant("2025-05-20", "15:00:00"), fechaFin = crearInstant("2025-05-20", "18:00:00"))

        val resultado = useCase(listOf(pTemprano, pCorrecto, pTarde), filtros)

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p_correcto", exito.programas[0].id)
    }

    // ============================================================================================
    // 4. COBERTURA ESTRUCTURAL (Decision / Condition Coverage)
    // ============================================================================================

    @Test
    fun `evalua todas las combinaciones posibles de estado del programa y fecha de inicio`() {
        // COBERTURA DE DECISIONES COMPUESTAS:
        // Expresión evaluada: `programa.estado == EstadoPrograma.DISPONIBLE && programa.fechaInicio.isAfter(ahora)`
        // Combinaciones probadas (Tabla de Verdad):
        // 1) DISPONIBLE (true)  && Futuro (true)  => Pasa el filtro
        // 2) DISPONIBLE (true)  && Pasado (false) => Rechazado
        // 3) NO_DISPONIBLE (false) && Futuro (true)  => Rechazado
        // 4) NO_DISPONIBLE (false) && Pasado (false) => Rechazado

        val p1 = crearPrograma(id = "p_true_true", estado = EstadoPrograma.DISPONIBLE, fechaInicio = crearInstant("2025-05-20"))
        val p2 = crearPrograma(id = "p_true_false", estado = EstadoPrograma.DISPONIBLE, fechaInicio = crearInstant("2025-05-10"))
        val p3 = crearPrograma(id = "p_false_true", estado = EstadoPrograma.NO_DISPONIBLE, fechaInicio = crearInstant("2025-05-20"))
        val p4 = crearPrograma(id = "p_false_false", estado = EstadoPrograma.NO_DISPONIBLE, fechaInicio = crearInstant("2025-05-10"))

        val resultado = useCase(listOf(p1, p2, p3, p4), FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(1, exito.programas.size)
        assertEquals("p_true_true", exito.programas[0].id)
    }

    @Test
    fun `evalua la primera rama de errorFechaHasta cuando fechaHasta es menor a hoy`() {
        // COBERTURA DE RAMAS (when):
        // Evaluamos la primera condición del `when` de `errorFechaHasta`:
        // `fechaHasta != null && fechaHasta.isBefore(hoy)`

        val fechaHastaInvalida = hoy.minusDays(2) // 2025-05-13
        val filtros = FiltrosProgramas(fechaHasta = fechaHastaInvalida)

        val resultado = useCase(emptyList(), filtros)

        assertTrue(resultado is ResultadoFiltrado.Invalido)
        val invalido = resultado as ResultadoFiltrado.Invalido
        assertEquals("La fecha hasta no puede ser anterior a hoy", invalido.errores.fechaHasta)
        assertNull(invalido.errores.fechaDesde)
        assertNull(invalido.errores.horaHasta)
    }

    @Test
    fun `evalua errores multiples simultaneos en la funcion de validacion`() {
        // COBERTURA DE MÚLTIPLES ERRORES DE CONDICIÓN:
        // Evaluamos la generación conjunta de `errorFechaDesde` y `errorHoraHasta`:
        // 1) fechaDesde = ayer -> Error "La fecha desde no puede ser anterior a hoy"
        // 2) horaDesde = 15:00, horaHasta = 10:00 -> Error "La hora final debe ser posterior a la inicial"

        val ayer = hoy.minusDays(1)
        val filtros = FiltrosProgramas(
            fechaDesde = ayer,
            horaDesde = LocalTime.of(15, 0),
            horaHasta = LocalTime.of(10, 0)
        )

        val resultado = useCase(emptyList(), filtros)

        assertTrue(resultado is ResultadoFiltrado.Invalido)
        val invalido = resultado as ResultadoFiltrado.Invalido
        assertNotNull(invalido.errores.fechaDesde)
        assertNotNull(invalido.errores.horaHasta)
        assertTrue(invalido.errores.hayErrores)
    }

    @Test
    fun `dado varios programas validos desordenados, los devuelve ordenados por fechaInicio`() {
        // COBERTURA DE ORDENAMIENTO (sortedBy { programa -> programa.fechaInicio }):
        // Verifica que la lista final devuelta en ResultadoFiltrado.Exito esté siempre ordenada
        // cronológicamente por la fecha de inicio del programa.

        val pTardio = crearPrograma(id = "p_tardio", fechaInicio = crearInstant("2025-05-25", "10:00:00"))
        val pTemprano = crearPrograma(id = "p_temprano", fechaInicio = crearInstant("2025-05-18", "10:00:00"))
        val pMedio = crearPrograma(id = "p_medio", fechaInicio = crearInstant("2025-05-20", "10:00:00"))

        val desordenados = listOf(pTardio, pTemprano, pMedio)

        val resultado = useCase(desordenados, FiltrosProgramas())

        assertTrue(resultado is ResultadoFiltrado.Exito)
        val exito = resultado as ResultadoFiltrado.Exito
        assertEquals(3, exito.programas.size)

        // Verificamos el orden ascendente: 18 de Mayo -> 20 de Mayo -> 25 de Mayo
        assertEquals("p_temprano", exito.programas[0].id)
        assertEquals("p_medio", exito.programas[1].id)
        assertEquals("p_tardio", exito.programas[2].id)
    }
}
