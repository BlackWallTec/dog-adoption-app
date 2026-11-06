package com.app.android.voluntariado

import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Inscripcion
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.app.android.domain.repoInterface.repoIntVoluntariado.InscripcionRepository
import kotlinx.coroutines.CompletableDeferred
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

/*
 * HERRAMIENTAS COMPARTIDAS
 * Este archivo no tiene pruebas. Tiene las piezas que todas las pruebas reutilizan.
 */

/*
 * Un RELOJ FIJO es un reloj detenido. Si usáramos la hora real, una prueba que hoy
 * pasa podría fallar la próxima semana, cuando la fecha del programa ya haya pasado.
 * Una buena prueba da siempre el mismo resultado, sin importar cuándo se corra.
 */
val AHORA: Instant = Instant.parse("2026-11-05T18:00:00Z")
val RELOJ_FIJO: Clock = Clock.fixed(AHORA, ZoneOffset.UTC)

/*
 * Un programa en el que SÍ te puedes inscribir: es nuestro "caso feliz".
 * Cada prueba cambia solo el dato que le interesa, por ejemplo:
 *     programaValido().copy(cupoDisponible = 0)
 * Así, si la prueba falla, sabemos que la causa fue ese único dato.
 */
fun programaValido(): Programa =
    Programa(
        id = "programa-1",
        nombre = "Paseo de perritos",
        tipo = "Paseo",
        descripcion = "Pasear perritos del refugio",
        fechaInicio = AHORA.plus(Duration.ofDays(7)),
        fechaFin = AHORA.plus(Duration.ofDays(14)),
        ubicacion = "Refugio",
        cupoMaximo = 10,
        cupoDisponible = 5,
        estado = EstadoPrograma.DISPONIBLE,
        usuarioInscrito = false,
    )

val INSCRIPCION_OK = Inscripcion("programa-1", AHORA, "Activa")

// Los 8 errores posibles. Los recorremos para no olvidar ninguno.
val TODOS_LOS_ERRORES: List<VoluntariadoError> =
    listOf(
        VoluntariadoError.SinCupo,
        VoluntariadoError.YaInscrito,
        VoluntariadoError.ProgramaIniciado,
        VoluntariadoError.ProgramaNoDisponible,
        VoluntariadoError.ProgramaNoEncontrado,
        VoluntariadoError.NoAutenticado,
        VoluntariadoError.SinConexion,
        VoluntariadoError.Desconocido,
    )

/*
 * Un FAKE (doble de prueba) es una versión de mentira de una dependencia.
 * Este reemplaza al repositorio real: no usa internet ni base de datos, responde lo
 * que le pidamos y anota cómo lo llamaron.
 *
 * `compuerta` sirve para simular una red lenta. Si existe, el repositorio se queda
 * esperando hasta que la prueba la abra. Así podemos revisar qué pasa MIENTRAS la
 * petición sigue en camino (por ejemplo, un doble toque en el botón).
 */
class RepositorioFalso(
    private val respuesta: Resultado<Inscripcion> = Resultado.Exito(INSCRIPCION_OK),
    private val compuerta: CompletableDeferred<Unit>? = null,
) : InscripcionRepository {
    val llamadas = mutableListOf<String>()

    override suspend fun inscribirse(idPrograma: String): Resultado<Inscripcion> {
        llamadas.add(idPrograma)
        compuerta?.await()
        return respuesta
    }
}
