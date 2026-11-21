package com.app.android

import com.app.android.domain.entities.entitiesDonativos.ComprobanteArchivo
import com.app.android.domain.entities.entitiesDonativos.ConstanciaArchivo
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.domain.repoInterface.repoIntDonativos.DatosFiscalesRepositoryInt
import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import com.app.android.domain.useCases.useCasesDonativos.GetDatosFiscalesUseCase
import com.app.android.domain.useCases.useCasesDonativos.GetMisDonativosUseCase
import com.app.android.domain.useCases.useCasesDonativos.GuardarDatosFiscalesUseCase
import com.app.android.domain.useCases.useCasesDonativos.RegistrarDonativoUseCase
import com.app.android.domain.useCases.useCasesDonativos.SolicitarCfdiUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

// Repos falsos: guardan lo que les llega para revisarlo, sin tocar el backend
private class DatosFiscalesRepoFake : DatosFiscalesRepositoryInt {
    var datosEnBd = DatosFiscales()
    var guardados: DatosFiscales? = null
    var constanciaSubida: ConstanciaArchivo? = null
    var fallaAlGuardar = false
    var fallaAlSubir = false

    override suspend fun getDatosFiscales() = datosEnBd

    override suspend fun guardarDatosFiscales(datos: DatosFiscales) {
        if (fallaAlGuardar) throw Exception("sin red")
        guardados = datos
    }

    override suspend fun subirConstancia(archivo: ConstanciaArchivo): String {
        if (fallaAlSubir) throw Exception("sin red")
        constanciaSubida = archivo
        return "usuario1/constancia.pdf"
    }
}

private class DonativosRepoFake : DonativosRepositoryInt {
    var monto: String? = null
    var referencia: String? = null
    var comprobante: ComprobanteArchivo? = null
    var llamadas = 0
    var falla = false
    var donativos: List<Donativo> = emptyList()
    var idCfdiPedido: String? = null

    override suspend fun getMisDonativos() = donativos

    override suspend fun solicitarCfdi(idDonativo: String) {
        if (falla) throw Exception("sin red")
        idCfdiPedido = idDonativo
    }

    override suspend fun registrarDonativo(
        monto: String,
        referencia: String?,
        comprobante: ComprobanteArchivo,
    ) {
        if (falla) throw Exception("sin red")
        llamadas++
        this.monto = monto
        this.referencia = referencia
        this.comprobante = comprobante
    }
}

class DatosFiscalesUseCasesTest {
    private val repo = DatosFiscalesRepoFake()
    private val guardar = GuardarDatosFiscalesUseCase(repo)
    private val get = GetDatosFiscalesUseCase(repo)

    private val constancia = ConstanciaArchivo("constancia.pdf", "application/pdf", byteArrayOf(1, 2, 3))
    private val datosBien =
        DatosFiscales(
            rfc = "GOMS990101AB1",
            razonSocial = "Sofía Gómez",
            regimenFiscal = "626",
            codigoPostal = "76000",
            correoFacturacion = "sofia@gmail.com",
            constanciaUrl = "usuario1/constancia.pdf",
        )

    // CA-01: los datos completos se guardan
    @Test
    fun `con todo bien guarda los datos y no marca errores`() =
        runBlocking {
            val errores = guardar(datosBien, null)
            assertFalse(errores.hayErrores)
            assertEquals(datosBien, repo.guardados)
        }

    @Test
    fun `limpia espacios y pasa el rfc a mayusculas antes de guardar`() =
        runBlocking {
            val sucios =
                datosBien.copy(
                    rfc = "  goms990101ab1 ",
                    razonSocial = " Sofía Gómez ",
                    codigoPostal = " 76000 ",
                    correoFacturacion = " sofia@gmail.com ",
                )
            guardar(sucios, null)
            assertEquals(datosBien, repo.guardados)
        }

    @Test
    fun `acepta rfc de empresa con 12 caracteres`() =
        runBlocking {
            val errores = guardar(datosBien.copy(rfc = "ABC990101AB1"), null)
            assertNull(errores.rfc)
        }

    @Test
    fun `sube la constancia nueva cuando escogio un archivo`() =
        runBlocking {
            val errores = guardar(datosBien.copy(constanciaUrl = null), constancia)
            assertFalse(errores.hayErrores)
            assertSame(constancia, repo.constanciaSubida)
        }

    @Test
    fun `si ya tenia constancia y no cambio no sube nada`() =
        runBlocking {
            guardar(datosBien, null)
            assertNull(repo.constanciaSubida)
        }

    // CA-02: si falta un dato se marca cuál y no se guarda
    @Test
    fun `con todo vacio marca cada campo y no guarda`() =
        runBlocking {
            val errores = guardar(DatosFiscales(), null)
            assertEquals("Escribe tu RFC", errores.rfc)
            assertNotNull(errores.razonSocial)
            assertNotNull(errores.regimenFiscal)
            assertNotNull(errores.codigoPostal)
            assertNotNull(errores.correoFacturacion)
            assertNotNull(errores.constancia)
            assertNull(repo.guardados)
        }

    @Test
    fun `rfc mal escrito marca solo el rfc`() =
        runBlocking {
            val errores = guardar(datosBien.copy(rfc = "ABC123"), null)
            assertEquals("Revisa tu RFC, debe tener 12 o 13 caracteres", errores.rfc)
            assertNull(errores.razonSocial)
            assertNull(errores.codigoPostal)
            assertNull(repo.guardados)
        }

    @Test
    fun `razon social con puros espacios cuenta como vacia`() =
        runBlocking {
            val errores = guardar(datosBien.copy(razonSocial = "   "), null)
            assertNotNull(errores.razonSocial)
            assertNull(repo.guardados)
        }

    @Test
    fun `codigo postal que no tiene 5 numeros se marca`() =
        runBlocking {
            assertNotNull(guardar(datosBien.copy(codigoPostal = "7600"), null).codigoPostal)
            assertNotNull(guardar(datosBien.copy(codigoPostal = "7600A"), null).codigoPostal)
            assertNull(repo.guardados)
        }

    @Test
    fun `correo sin arroba o sin dominio se marca`() =
        runBlocking {
            assertNotNull(guardar(datosBien.copy(correoFacturacion = "sofia.com"), null).correoFacturacion)
            assertNotNull(guardar(datosBien.copy(correoFacturacion = "sofia@gmail"), null).correoFacturacion)
            assertNull(repo.guardados)
        }

    @Test
    fun `sin constancia nueva ni guardada no deja guardar`() =
        runBlocking {
            val errores = guardar(datosBien.copy(constanciaUrl = null), null)
            assertEquals("Sube tu constancia de situación fiscal", errores.constancia)
            assertNull(repo.guardados)
        }

    @Test
    fun `si falla el guardado no intenta subir la constancia`() {
        repo.fallaAlGuardar = true
        assertThrows(Exception::class.java) {
            runBlocking { guardar(datosBien, constancia) }
        }
        assertNull(repo.constanciaSubida)
    }

    @Test
    fun `si falla la subida el error le llega a la pantalla`() {
        repo.fallaAlSubir = true
        assertThrows(Exception::class.java) {
            runBlocking { guardar(datosBien, constancia) }
        }
    }

    @Test
    fun `get regresa lo que hay en la bd`() =
        runBlocking {
            repo.datosEnBd = datosBien
            assertEquals(datosBien, get())
        }

    @Test
    fun `completos es false si falta la constancia o algun dato`() {
        assertTrue(datosBien.completos)
        assertFalse(datosBien.copy(constanciaUrl = null).completos)
        assertFalse(datosBien.copy(rfc = "").completos)
        assertFalse(DatosFiscales().completos)
    }
}

class RegistrarDonativoUseCaseTest {
    private val repo = DonativosRepoFake()
    private val registrar = RegistrarDonativoUseCase(repo)
    private val comprobante = ComprobanteArchivo("pago.jpg", "image/jpeg", byteArrayOf(1, 2, 3))

    // CA-01: el donativo se manda con su comprobante
    @Test
    fun `manda el monto la referencia y el comprobante`() =
        runBlocking {
            registrar("500.50", "ABC123", comprobante)
            assertEquals(1, repo.llamadas)
            assertEquals("500.50", repo.monto)
            assertEquals("ABC123", repo.referencia)
            assertSame(comprobante, repo.comprobante)
        }

    @Test
    fun `limpia los espacios de la referencia`() =
        runBlocking {
            registrar("500", "  ABC123 ", comprobante)
            assertEquals("ABC123", repo.referencia)
        }

    @Test
    fun `referencia vacia se manda como null porque es opcional`() =
        runBlocking {
            registrar("500", "   ", comprobante)
            assertEquals(1, repo.llamadas)
            assertNull(repo.referencia)
        }

    @Test
    fun `si falla el envio el error le llega a la pantalla`() {
        repo.falla = true
        assertThrows(Exception::class.java) {
            runBlocking { registrar("500", "", comprobante) }
        }
        assertEquals(0, repo.llamadas)
    }
}

// HU49: mis donativos y pedir CFDI
class MisDonativosCfdiTest {
    private val repo = DonativosRepoFake()
    private val getMisDonativos = GetMisDonativosUseCase(repo)
    private val solicitarCfdi = SolicitarCfdiUseCase(repo)

    @Test
    fun `CA-01 un donativo validado sin CFDI deja pedirlo`() {
        assertTrue(Donativo("1", 500.0, "2026-11-19", "Validado", false).puedePedirCfdi)
    }

    @Test
    fun `CA-02 un donativo pendiente no deja pedir CFDI`() {
        assertFalse(Donativo("2", 500.0, "2026-11-19", "Pendiente de validación", false).puedePedirCfdi)
    }

    @Test
    fun `no deja pedir el CFDI dos veces`() {
        assertFalse(Donativo("3", 500.0, "2026-11-19", "Validado", true).puedePedirCfdi)
    }

    @Test
    fun `trae los donativos del repositorio`() =
        runBlocking {
            repo.donativos = listOf(Donativo("1", 1.0, "2026-11-19", "Validado", false))
            assertEquals(1, getMisDonativos().size)
        }

    @Test
    fun `manda el id del donativo al pedir el CFDI`() =
        runBlocking {
            solicitarCfdi("abc")
            assertEquals("abc", repo.idCfdiPedido)
        }

    @Test
    fun `si falla la red el error le llega a la pantalla`() {
        repo.falla = true
        assertThrows(Exception::class.java) {
            runBlocking { solicitarCfdi("abc") }
        }
    }
}
