package com.app.android.voluntariado

import com.app.android.data.dtos.dtosVoluntariado.DetalleProgramaDto
import com.app.android.data.repoImp.repoImpVoluntariado.toDomain
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.MarcaPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.accion
import com.app.android.presentation.view.viewsVoluntariado.cupoTexto
import com.app.android.presentation.view.viewsVoluntariado.fechaDetalleTexto
import com.app.android.presentation.view.viewsVoluntariado.progresoCupo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Duration

/*
 * PRUEBAS DEL DETALLE DE UN PROGRAMA (HU36)
 *
 * 1. La regla del botón: accion(ahora) decide qué ve el voluntario en lugar del botón.
 *    El orden importa: cancelado, finalizado, ya inscrito, en curso, cupo lleno, inscribirme.
 * 2. Los textos del detalle (cupo y fecha).
 * 3. El mapper: del JSON del backend a la entidad.
 */

// Un detalle en el que SÍ te puedes inscribir. Cada prueba cambia solo lo que le interesa.
fun detalleDePrueba(
    programa: Programa = programaValido(),
    inscritos: Int = 5,
    estadoGuardado: String? = "Disponible",
): DetallePrograma = DetallePrograma(programa, "Paseos con los perros", inscritos, estadoGuardado)

class DetalleProgramaTest {
    // ------------------------------------------------------------------------------
    // 1. REGLA DEL BOTÓN
    // ------------------------------------------------------------------------------

    @Test
    fun `caso feliz - disponible, con lugar y sin empezar muestra Inscribirme`() {
        assertEquals(AccionDetalle.Inscribirme, detalleDePrueba().accion(AHORA))
    }

    @Test
    fun `prioridad - cancelado gana aunque el usuario estuviera inscrito`() {
        val detalle =
            detalleDePrueba(
                programa = programaValido().copy(usuarioInscrito = true),
                estadoGuardado = "Cancelado",
            )

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.CANCELADO), detalle.accion(AHORA))
    }

    @Test
    fun `nulos - el estado guardado se compara sin importar mayúsculas`() {
        val detalle = detalleDePrueba(estadoGuardado = "CANCELADO")

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.CANCELADO), detalle.accion(AHORA))
    }

    @Test
    fun `finalizado - por estado guardado`() {
        val detalle = detalleDePrueba(estadoGuardado = "Finalizado")

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.FINALIZADO), detalle.accion(AHORA))
    }

    @Test
    fun `valor límite - justo a la hora de fin ya está finalizado`() {
        val programa = programaValido().copy(fechaInicio = AHORA.minus(Duration.ofHours(3)), fechaFin = AHORA)

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.FINALIZADO), detalleDePrueba(programa).accion(AHORA))
    }

    @Test
    fun `valor límite - un segundo antes del fin sigue en curso`() {
        val programa =
            programaValido().copy(
                fechaInicio = AHORA.minus(Duration.ofHours(3)),
                fechaFin = AHORA.plusSeconds(1),
            )

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.EN_CURSO), detalleDePrueba(programa).accion(AHORA))
    }

    @Test
    fun `valor límite - justo a la hora de inicio ya está en curso`() {
        val programa = programaValido().copy(fechaInicio = AHORA)

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.EN_CURSO), detalleDePrueba(programa).accion(AHORA))
    }

    @Test
    fun `ya inscrito - muestra Ya estás inscrito`() {
        val detalle = detalleDePrueba(programa = programaValido().copy(usuarioInscrito = true))

        assertEquals(AccionDetalle.YaInscrito, detalle.accion(AHORA))
    }

    @Test
    fun `cupo lleno - sin lugares muestra Cupo lleno`() {
        val detalle = detalleDePrueba(programa = programaValido().copy(cupoDisponible = 0))

        assertEquals(AccionDetalle.CupoLleno, detalle.accion(AHORA))
    }

    @Test
    fun `no disponible - un estado distinto de Disponible (por ejemplo Cerrado) no deja inscribirse`() {
        val detalle =
            detalleDePrueba(
                programa = programaValido().copy(estado = EstadoPrograma.NO_DISPONIBLE),
                estadoGuardado = "Cerrado",
            )

        assertEquals(AccionDetalle.SinAccion(MarcaPrograma.NO_DISPONIBLE), detalle.accion(AHORA))
    }

    // ------------------------------------------------------------------------------
    // 2. TEXTOS DEL DETALLE
    // ------------------------------------------------------------------------------

    @Test
    fun `cupo - con límite muestra ocupados y disponibles`() {
        val detalle = detalleDePrueba(programa = programaValido().copy(cupoMaximo = 15), inscritos = 8)

        assertEquals("8/15 lugares ocupados · 7 disponibles", detalle.cupoTexto())
    }

    @Test
    fun `cupo - sin límite lo dice en lugar de inventar un número`() {
        val detalle = detalleDePrueba(programa = programaValido().copy(cupoMaximo = null), inscritos = 3)

        assertEquals("3 inscritos · Sin límite de lugares", detalle.cupoTexto())
        assertEquals(0f, detalle.progresoCupo())
    }

    @Test
    fun `valor límite - más inscritos que cupo no da disponibles negativos ni barra mayor a 1`() {
        val detalle = detalleDePrueba(programa = programaValido().copy(cupoMaximo = 10), inscritos = 12)

        assertEquals("12/10 lugares ocupados · 0 disponibles", detalle.cupoTexto())
        assertEquals(1f, detalle.progresoCupo())
    }

    @Test
    fun `fecha - un solo día no muestra rango y varios días sí`() {
        val unDia = programaValido().copy(fechaFin = programaValido().fechaInicio.plus(Duration.ofHours(2)))
        val variosDias = programaValido().copy(fechaFin = programaValido().fechaInicio.plus(Duration.ofDays(5)))

        assertFalse(detalleDePrueba(unDia).fechaDetalleTexto().contains("–"))
        assertTrue(detalleDePrueba(variosDias).fechaDetalleTexto().contains("–"))
    }

    // ------------------------------------------------------------------------------
    // 3. MAPPER: JSON DEL BACKEND -> ENTIDAD
    // ------------------------------------------------------------------------------

    private fun dto(
        cupoMaximo: Int? = 20,
        inscritos: Int? = 8,
        estado: String? = "Disponible          ",
        tipoDescripcion: String? = "Actividades de apoyo",
    ) = DetalleProgramaDto(
        idPrograma = "programa-1",
        nombre = "Paseos",
        tipo = "Voluntariado",
        tipoDescripcion = tipoDescripcion,
        descripcion = "Ropa cómoda.\nAgua.",
        fechaInicio = "2026-12-10T09:00:00",
        fechaFin = "2026-12-10T13:00:00",
        ubicacion = "Refugio",
        cupoMaximo = cupoMaximo,
        estadoDelPrograma = estado,
        inscritos = inscritos,
        usuarioInscrito = true,
    )

    @Test
    fun `mapper - calcula el cupo disponible con los inscritos y limpia el estado`() {
        val detalle = dto().toDomain()

        assertEquals(12, detalle.programa.cupoDisponible)
        assertEquals("Disponible", detalle.estadoGuardado)
        assertEquals(EstadoPrograma.DISPONIBLE, detalle.programa.estado)
        assertTrue(detalle.programa.usuarioInscrito)
        assertEquals("Ropa cómoda.\nAgua.", detalle.programa.descripcion)
    }

    @Test
    fun `mapper - nulos y vacíos`() {
        val detalle = dto(cupoMaximo = null, inscritos = null, tipoDescripcion = "   ").toDomain()

        assertEquals(0, detalle.inscritos)
        assertNull(detalle.programa.cupoDisponible)
        assertNull(detalle.tipoDescripcion)
    }

    @Test
    fun `mapper - con más inscritos que cupo el disponible no baja de cero`() {
        assertEquals(0, dto(cupoMaximo = 5, inscritos = 9).toDomain().programa.cupoDisponible)
    }
}
