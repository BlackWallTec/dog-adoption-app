package com.app.android

import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.domain.useCases.useCasesSolicitudes.CalcularEdadUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GetDatosGeneralesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarDatosGeneralesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarSolicitudUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SolicitudesUseCasesTest {
    // Cada test usa el fake directamente, no se usa Hilt
    private val repo = SolicitudRepositoryFake()
    private val iniciarSolicitud = IniciarSolicitudUseCase(repo)
    private val getDatosGenerales = GetDatosGeneralesUseCase(repo)
    private val guardarDatosGenerales = GuardarDatosGeneralesUseCase(repo)
    private val calcularEdad = CalcularEdadUseCase()

    @Test
    fun `si ya hay borrador se continua el mismo`() =
        runBlocking {
            val primera = iniciarSolicitud("usuario1", "perro1")
            val segunda = iniciarSolicitud("usuario1", "perro1")
            assertEquals(primera.id, segunda.id) // no crea otra
        }

    @Test
    fun `sin datos guardados precarga del perfil`() =
        runBlocking {
            val solicitud = iniciarSolicitud("usuario1", "perro1")
            val datos = getDatosGenerales(solicitud.id, "usuario1")
            assertEquals("Sofía García López", datos.nombreCompleto)
        }

    @Test
    fun `ocupacion vacia no deja guardar`() =
        runBlocking {
            val solicitud = iniciarSolicitud("usuario1", "perro1")
            val datos = getDatosGenerales(solicitud.id, "usuario1") // viene sin ocupación
            val errores = guardarDatosGenerales(solicitud.id, datos)
            assertTrue(errores.hayErrores)
        }

    @Test
    fun `con todo bien se guarda y se lee lo guardado`() =
        runBlocking {
            val solicitud = iniciarSolicitud("usuario1", "perro1")
            val datos = getDatosGenerales(solicitud.id, "usuario1").copy(ocupacion = "Ingeniera")
            val errores = guardarDatosGenerales(solicitud.id, datos)
            assertFalse(errores.hayErrores)

            val leidos = getDatosGenerales(solicitud.id, "usuario1")
            assertEquals("Ingeniera", leidos.ocupacion) // ya no es el del perfil
        }

    @Test
    fun `calcula la edad antes y despues del cumpleanos`() {
        val hoy = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 14) } // un día antes
        assertEquals(30, calcularEdad("1995-03-15", hoy))
        hoy.set(2026, Calendar.MARCH, 15) // el día del cumpleaños
        assertEquals(31, calcularEdad("1995-03-15", hoy))
    }
}