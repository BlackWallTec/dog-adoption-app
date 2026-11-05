package com.app.android

import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudRepositoryFake
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.useCases.useCasesSolicitudes.CalcularEdadUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GetDatosGeneralesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GuardarDatosGeneralesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarBorradorUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ResultadoInicio
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class SolicitudesUseCasesTest {
    // Cada test arma sus piezas a mano, sin Hilt
    private val servidor = SolicitudRepositoryFake()
    private val borrador = BorradorRepositoryImpl()
    private val iniciar = IniciarBorradorUseCase(servidor, borrador)
    private val getDatosGenerales = GetDatosGeneralesUseCase(servidor, borrador)
    private val guardarDatosGenerales = GuardarDatosGeneralesUseCase(borrador)
    private val calcularEdad = CalcularEdadUseCase()

    @Test
    fun `sin solicitud activa inicia el borrador`() =
        runBlocking {
            val resultado = iniciar("usuario1", "perro1")
            assertEquals(ResultadoInicio.Lista, resultado)
            assertEquals("perro1", borrador.borrador.value?.idPerro)
        }

    @Test
    fun `con solicitud activa bloquea y no inicia borrador`() =
        runBlocking {
            servidor.agregarActiva("usuario1", "perro1", SolicitudResumen("sol-1", EstadoSolicitud.PENDIENTE_REVISION))
            val resultado = iniciar("usuario1", "perro1")
            assertTrue(resultado is ResultadoInicio.Bloqueada)
            assertNull(borrador.borrador.value) // no se tocó el borrador
        }

    @Test
    fun `sin datos en el borrador precarga del perfil`() =
        runBlocking {
            iniciar("usuario1", "perro1")
            val datos = getDatosGenerales()
            assertEquals("Sofía García López", datos.nombreCompleto)
        }

    @Test
    fun `ocupacion vacia no se guarda en el borrador`() =
        runBlocking {
            iniciar("usuario1", "perro1")
            val datos = getDatosGenerales() // viene sin ocupación
            val errores = guardarDatosGenerales(datos)
            assertTrue(errores.hayErrores)
            assertNull(borrador.borrador.value?.datosGenerales)
        }

    @Test
    fun `al regresar se lee lo guardado en el borrador y no el perfil`() =
        runBlocking {
            iniciar("usuario1", "perro1")
            val datos = getDatosGenerales().copy(ocupacion = "Ingeniera")
            guardarDatosGenerales(datos)

            val otraVez = getDatosGenerales() // como si regresara con back
            assertEquals("Ingeniera", otraVez.ocupacion)
        }

    @Test
    fun `reiniciar con el mismo perro conserva el borrador`() =
        runBlocking {
            iniciar("usuario1", "perro1")
            guardarDatosGenerales(getDatosGenerales().copy(ocupacion = "Ingeniera"))

            iniciar("usuario1", "perro1") // regresó a Requisitos y volvió a picar Continuar
            assertEquals("Ingeniera", borrador.borrador.value?.datosGenerales?.ocupacion)
        }

    @Test
    fun `iniciar con otro perro empieza un borrador limpio`() =
        runBlocking {
            iniciar("usuario1", "perro1")
            guardarDatosGenerales(getDatosGenerales().copy(ocupacion = "Ingeniera"))

            iniciar("usuario1", "perro2")
            assertNull(borrador.borrador.value?.datosGenerales)
        }

    @Test
    fun `calcula la edad antes y despues del cumpleanos`() {
        val hoy = Calendar.getInstance().apply { set(2026, Calendar.MARCH, 14) }
        assertEquals(30, calcularEdad("1995-03-15", hoy))
        hoy.set(2026, Calendar.MARCH, 15)
        assertEquals(31, calcularEdad("1995-03-15", hoy))
    }
}