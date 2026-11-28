package com.app.android.solicitudes

import com.app.android.data.TokenProvider
import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudHttpException
import com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.DocumentoLocal
import com.app.android.domain.entities.entitiesSolicitudes.DocumentosSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.entities.entitiesSolicitudes.PickedFile
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudCliente
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudParaEditar
import com.app.android.domain.entities.entitiesSolicitudes.SolicitudResumen
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.entities.entitiesSolicitudes.UploadedDocument
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import com.app.android.domain.useCases.useCasesSolicitudes.EnviarSolicitudUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.ValidarBorradorUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test

class EnviarSolicitudUseCaseTest {
    private class FakeSolicitudRepository(
        var shouldFail: Boolean = false,
        var failCode: Int = 400,
        var failMessage: String = "Error",
    ) : SolicitudRepositoryInt {
        var createCalled = false
        var uploadedDocs = mutableListOf<DocumentType>()
        var finalizeCalled = false

        var reenviarCalled = false
        var reenviarId: String? = null

        override suspend fun getSolicitudParaEditar(idSolicitud: String): SolicitudParaEditar =
            throw NotImplementedError("No se usa en estos tests")

        override suspend fun reenviarSolicitud(
            idSolicitud: String,
            borrador: BorradorSolicitud,
        ) {
            if (shouldFail) throw SolicitudHttpException(failCode, failMessage)
            reenviarCalled = true
            reenviarId = idSolicitud
        }

        override suspend fun getSolicitudActiva(idPerro: String): SolicitudResumen? = null

        override suspend fun getMisSolicitudes(): List<SolicitudCliente> = emptyList()

        override suspend fun getPerfil(): DatosGenerales =
            DatosGenerales(
                nombreCompleto = "Test",
                fechaNacimiento = "1995-03-15",
                ocupacion = "Dev",
                domicilio = "Calle",
                municipio = "Qro",
                telefono = "123",
                correo = "test@correo.com",
            )

        override suspend fun crearOReutilizarBorrador(
            borrador: com.app.android.domain.entities.entitiesSolicitudes.BorradorSolicitud,
        ): String {
            if (shouldFail) throw SolicitudHttpException(failCode, failMessage)
            createCalled = true
            return "sol-test-999"
        }

        override suspend fun subirDocumento(
            solicitudId: String,
            type: DocumentType,
            file: PickedFile,
            bytes: ByteArray,
        ): UploadedDocument {
            if (shouldFail) throw SolicitudHttpException(failCode, failMessage)
            uploadedDocs.add(type)
            return UploadedDocument(type = type, fileName = file.name, sizeBytes = file.sizeBytes)
        }

        override suspend fun finalizarSolicitud(solicitudId: String) {
            if (shouldFail) throw SolicitudHttpException(failCode, failMessage)
            finalizeCalled = true
        }
    }

    private class FakeTokenProvider : TokenProvider {
        var tokenCleared = false

        override fun token(): String? = "token123"

        override fun save(token: String): Boolean = true

        override fun clear() {
            tokenCleared = true
        }
    }

    private fun prepararBorradorCompletoValido(borradorRepo: BorradorRepositoryImpl) {
        borradorRepo.iniciar("user1", "dog1")
        borradorRepo.actualizar {
            it.copy(
                datosGenerales =
                    DatosGenerales(
                        nombreCompleto = "Sofía",
                        fechaNacimiento = "1995-03-15",
                        ocupacion = "Ingeniera",
                        domicilio = "Calle",
                        municipio = "Qro",
                        telefono = "123",
                        correo = "sofia@correo.com",
                    ),
                tuHogar =
                    TuHogar(
                        hadPet = false,
                        otherPets = false,
                        whyAdopt = "Quiero compañía",
                        whereLive = "En casa",
                        peopleInHouse = 2,
                        allAgree = true,
                        houseType = HouseType.CASA,
                        hasGarden = "Sí",
                        aboutPets = "",
                    ),
                compromiso =
                    Compromiso(
                        petNeeds = "Comida y agua",
                        petSterilization = "Muy importante",
                        hasVet = true,
                        vetDetail = "Dr. Pérez",
                        timeAlone = "4 horas",
                        dailyRoutine = "Paseo diario",
                        dogManagement = "Entrenamiento positivo",
                    ),
                documentos =
                    DocumentosSolicitud(
                        identificacion =
                            DocumentoLocal(
                                uri = "content://ine",
                                nombre = "ine.pdf",
                                tamanoBytes = 100L,
                                mimeType = "application/pdf",
                            ),
                        comprobanteDomicilio =
                            DocumentoLocal(
                                uri = "content://dom",
                                nombre = "dom.pdf",
                                tamanoBytes = 100L,
                                mimeType = "application/pdf",
                            ),
                    ),
            )
        }
    }

    @Test
    fun `enviar solicitud exitoso ejecuta secuencia completa y limpia borrador`() =
        runBlocking {
            val borradorRepo = BorradorRepositoryImpl()
            val repo = FakeSolicitudRepository()
            val tokenProv = FakeTokenProvider()

            prepararBorradorCompletoValido(borradorRepo)

            val useCase =
                EnviarSolicitudUseCase(
                    context = mockkContext(),
                    borradorRepo = borradorRepo,
                    solicitudRepo = repo,
                    validarBorradorUseCase = ValidarBorradorUseCase(borradorRepo),
                    tokenProvider = tokenProv,
                    fileReader = { "test bytes".toByteArray() },
                )

            val id = useCase()

            assertEquals("sol-test-999", id)
            assertNull(borradorRepo.borrador.value)
            assertEquals(true, repo.createCalled)
            assertEquals(listOf(DocumentType.INE, DocumentType.DOMICILIO), repo.uploadedDocs)
            assertEquals(true, repo.finalizeCalled)
        }

    @Test
    fun `falla de red al enviar mantiene borrador intacto`() =
        runBlocking {
            val borradorRepo = BorradorRepositoryImpl()
            val repo = FakeSolicitudRepository(shouldFail = true, failCode = 409, failMessage = "Solicitud activa existente")
            val tokenProv = FakeTokenProvider()

            prepararBorradorCompletoValido(borradorRepo)

            val useCase =
                EnviarSolicitudUseCase(
                    context = mockkContext(),
                    borradorRepo = borradorRepo,
                    solicitudRepo = repo,
                    validarBorradorUseCase = ValidarBorradorUseCase(borradorRepo),
                    tokenProvider = tokenProv,
                    fileReader = { "test bytes".toByteArray() },
                )

            try {
                useCase()
                fail("Debería haber lanzado excepción")
            } catch (e: SolicitudHttpException) {
                assertEquals(409, e.statusCode)
                assertEquals("Solicitud activa existente", e.message)
                assertNotNull(borradorRepo.borrador.value)
            }
        }

    @Test
    fun `error 401 limpia token y preserva borrador`() =
        runBlocking {
            val borradorRepo = BorradorRepositoryImpl()
            val repo = FakeSolicitudRepository(shouldFail = true, failCode = 401, failMessage = "Unauthorized")
            val tokenProv = FakeTokenProvider()

            prepararBorradorCompletoValido(borradorRepo)

            val useCase =
                EnviarSolicitudUseCase(
                    context = mockkContext(),
                    borradorRepo = borradorRepo,
                    solicitudRepo = repo,
                    validarBorradorUseCase = ValidarBorradorUseCase(borradorRepo),
                    tokenProvider = tokenProv,
                    fileReader = { "test bytes".toByteArray() },
                )

            try {
                useCase()
            } catch (_: SolicitudHttpException) {
            }

            assertEquals(true, tokenProv.tokenCleared)
            assertNotNull(borradorRepo.borrador.value)
        }

    private fun mockkContext(): android.content.Context = object : android.content.ContextWrapper(null as android.content.Context?) {}
}
