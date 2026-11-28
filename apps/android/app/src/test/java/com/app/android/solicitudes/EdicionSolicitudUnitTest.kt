package com.app.android.solicitudes

import com.app.android.data.TokenProvider
import com.app.android.data.dtos.dtosSolicitudes.SolicitudEdicionDto
import com.app.android.data.dtos.dtosSolicitudes.toDomain
import com.app.android.data.repoImp.repoImpSolicitudes.BorradorRepositoryImpl
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
import com.app.android.domain.repoInterface.repoIntSolicitudes.NoEditableException
import com.app.android.domain.repoInterface.repoIntSolicitudes.SolicitudRepositoryInt
import com.app.android.domain.useCases.useCasesSolicitudes.EnviarSolicitudUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GetEdicionUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.GetMisSolicitudesUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.IniciarEdicionUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.SinCambiosException
import com.app.android.domain.useCases.useCasesSolicitudes.ValidacionBorradorResult
import com.app.android.domain.useCases.useCasesSolicitudes.ValidarBorradorUseCase
import com.app.android.presentation.viewmodel.viewModelSolicitudes.EdicionUiState
import com.app.android.presentation.viewmodel.viewModelSolicitudes.MisSolicitudesViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test

/**
 * Pruebas de HU09: editar y reenviar una solicitud rechazada.
 * Se agrupan por categoría igual que el resto de tests del proyecto.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class EdicionSolicitudUnitTest {
    // ----------------------------------------------------------------------------
    // FAKES (repositorio falso que NO usa red; guarda lo que se le pide)
    // ----------------------------------------------------------------------------
    private class FakeRepo : SolicitudRepositoryInt {
        var paraEditar: SolicitudParaEditar? = null
        var errorAlEditar: Exception? = null
        var errorAlReenviar: Exception? = null

        val docsSubidos = mutableListOf<DocumentType>()
        var reenviarCalled = false
        var reenviarId: String? = null

        override suspend fun getSolicitudActiva(idPerro: String): SolicitudResumen? = null

        override suspend fun getMisSolicitudes(): List<SolicitudCliente> = emptyList()

        override suspend fun getPerfil(): DatosGenerales = perfil()

        override suspend fun crearOReutilizarBorrador(borrador: BorradorSolicitud): String = "no-se-usa"

        override suspend fun subirDocumento(
            solicitudId: String,
            type: DocumentType,
            file: PickedFile,
            bytes: ByteArray,
        ): UploadedDocument {
            docsSubidos.add(type)
            return UploadedDocument(type = type, fileName = file.name, sizeBytes = file.sizeBytes)
        }

        override suspend fun finalizarSolicitud(solicitudId: String) = Unit

        override suspend fun getSolicitudParaEditar(idSolicitud: String): SolicitudParaEditar {
            errorAlEditar?.let { throw it }
            return paraEditar ?: throw IllegalStateException("Falta configurar paraEditar")
        }

        override suspend fun reenviarSolicitud(
            idSolicitud: String,
            borrador: BorradorSolicitud,
        ) {
            errorAlReenviar?.let { throw it }
            reenviarCalled = true
            reenviarId = idSolicitud
        }
    }

    private class FakeToken : TokenProvider {
        override fun token(): String? = "t"

        override fun save(token: String): Boolean = true

        override fun clear() = Unit
    }

    // ----------------------------------------------------------------------------
    // DATOS DE PRUEBA
    // ----------------------------------------------------------------------------
    private companion object {
        fun perfil() =
            DatosGenerales(
                nombreCompleto = "Sofía",
                fechaNacimiento = "1995-03-15",
                ocupacion = "",
                domicilio = "Calle",
                municipio = "Qro",
                telefono = "123",
                correo = "sofia@correo.com",
            )

        fun hogar() =
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
            )

        fun compromiso() =
            Compromiso(
                petNeeds = "Comida y agua",
                petSterilization = "Muy importante",
                hasVet = true,
                vetDetail = "Dr. Pérez",
                timeAlone = "4 horas",
                dailyRoutine = "Paseo diario",
                dogManagement = "Entrenamiento positivo",
            )

        fun docLocal(nombre: String) =
            DocumentoLocal(
                uri = "content://$nombre",
                nombre = nombre,
                tamanoBytes = 100L,
                mimeType = "application/pdf",
            )

        fun solicitudParaEditar(
            tieneIne: Boolean = true,
            tieneDomicilio: Boolean = true,
        ) = SolicitudParaEditar(
            idSolicitud = "sol-1",
            idPerro = "dog1",
            ocupacion = "Ingeniera",
            tuHogar = hogar(),
            compromiso = compromiso(),
            tieneIne = tieneIne,
            tieneDomicilio = tieneDomicilio,
        )

        fun dtoBase() =
            SolicitudEdicionDto(
                idSolicitud = "sol-1",
                idPerro = "dog1",
                ocupacion = "Ingeniera",
                tuvoMascotas = false,
                otrasMascotas = false,
                motivoAdopcion = "Compañía",
                dondeVivira = "Casa",
                personasEnCasa = 3,
                todosDeAcuerdo = true,
                tipoVivienda = "Casa",
                jardinPatio = "Sí",
                caseroAcepta = null,
                sobreMascotas = null,
                cuidadosBasicos = "Comida",
                opinionEsterilizacion = "Sí",
                tieneVeterinario = true,
                detalleVeterinario = "Dr. X",
                tiempoSolo = "2h",
                rutinaDiaria = "Paseo",
                manejoComportamiento = "Positivo",
                tieneIne = true,
                tieneDomicilio = false,
            )
    }

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun contexto(): android.content.Context = object : android.content.ContextWrapper(null as android.content.Context?) {}

    private fun crearEnviar(
        borradorRepo: BorradorRepositoryImpl,
        repo: FakeRepo,
    ) = EnviarSolicitudUseCase(
        context = contexto(),
        borradorRepo = borradorRepo,
        solicitudRepo = repo,
        validarBorradorUseCase = ValidarBorradorUseCase(borradorRepo),
        tokenProvider = FakeToken(),
        fileReader = { "bytes".toByteArray() },
    )

    /** Deja el borrador en modo edición, como si ya hubieras pulsado "Editar". */
    private fun prepararEdicion(
        repo: FakeRepo,
        borradorRepo: BorradorRepositoryImpl,
        tieneIne: Boolean = true,
        tieneDomicilio: Boolean = true,
    ) = runBlocking {
        repo.paraEditar = solicitudParaEditar(tieneIne, tieneDomicilio)
        IniciarEdicionUseCase(repo, borradorRepo)("user1", "sol-1")
    }

    // ============================================================================
    // SECCIÓN 1: IniciarEdicionUseCase (decision_condition / structural)
    // ============================================================================
    @Test
    fun `structural_iniciar_edicion_construye_borrador_con_edicion_y_original`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)

        val b = borradorRepo.borrador.value!!
        assertEquals("user1", b.idUsuario)
        assertEquals("dog1", b.idPerro)
        assertTrue("Debe venir con requisitos aceptados", b.aceptoRequisitos)
        assertEquals("La ocupación sale de la solicitud, no del perfil", "Ingeniera", b.datosGenerales?.ocupacion)
        assertEquals("Sofía", b.datosGenerales?.nombreCompleto)
        assertNotNull(b.edicion)
        assertEquals("sol-1", b.edicion?.idSolicitud)
        assertEquals(hogar(), b.edicion?.original?.tuHogar)
        assertNull("Los documentos empiezan sin tocar", b.documentos)
    }

    @Test
    fun `decision_condition_iniciar_edicion_refleja_que_documentos_hay_en_el_servidor`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo, tieneIne = true, tieneDomicilio = false)

        val edicion = borradorRepo.borrador.value!!.edicion!!
        assertTrue(edicion.tieneIneEnServidor)
        assertFalse(edicion.tieneDomicilioEnServidor)
    }

    @Test
    fun `void_value_iniciar_edicion_no_editable_no_toca_el_borrador`() {
        val repo = FakeRepo().apply { errorAlEditar = NoEditableException("ya no") }
        val borradorRepo = BorradorRepositoryImpl()

        try {
            runBlocking { IniciarEdicionUseCase(repo, borradorRepo)("user1", "sol-1") }
            fail("Debe propagar NoEditableException")
        } catch (e: NoEditableException) {
            assertNull(borradorRepo.borrador.value)
        }
    }

    @Test
    fun `structural_get_edicion_devuelve_null_en_solicitud_nueva_y_valor_en_edicion`() {
        val borradorRepo = BorradorRepositoryImpl()
        val getEdicion = GetEdicionUseCase(borradorRepo)

        borradorRepo.iniciar("user1", "dog1")
        assertNull("Solicitud nueva → sin edición", getEdicion())

        prepararEdicion(FakeRepo(), borradorRepo)
        assertEquals("sol-1", getEdicion()?.idSolicitud)
    }

    // ============================================================================
    // SECCIÓN 2: BorradorRepositoryImpl.reemplazar (boundary)
    // ============================================================================
    @Test
    fun `boundary_reemplazar_sobrescribe_aunque_ya_exista_un_borrador_del_mismo_perro`() {
        val borradorRepo = BorradorRepositoryImpl()
        borradorRepo.iniciar("user1", "dog1")
        borradorRepo.actualizar { it.copy(aceptoRequisitos = false) }

        borradorRepo.reemplazar(BorradorSolicitud(idUsuario = "user1", idPerro = "dog1", aceptoRequisitos = true))

        assertTrue(borradorRepo.borrador.value!!.aceptoRequisitos)
    }

    // ============================================================================
    // SECCIÓN 3: ValidarBorradorUseCase con documentos ya en el servidor
    // ============================================================================
    @Test
    fun `decision_condition_validar_acepta_edicion_sin_documentos_nuevos_si_ya_estan_en_servidor`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo, tieneIne = true, tieneDomicilio = true)

        assertTrue(ValidarBorradorUseCase(borradorRepo)() is ValidacionBorradorResult.Valido)
    }

    @Test
    fun `decision_condition_validar_rechaza_edicion_si_falta_un_documento_que_no_esta_en_servidor`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo, tieneIne = true, tieneDomicilio = false)

        assertTrue(ValidarBorradorUseCase(borradorRepo)() is ValidacionBorradorResult.Invalido)
    }

    // ============================================================================
    // SECCIÓN 4: EnviarSolicitudUseCase en modo edición
    // ============================================================================
    @Test
    fun `decision_condition_reenviar_sin_cambios_lanza_SinCambiosException_y_conserva_borrador`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)

        try {
            runBlocking { crearEnviar(borradorRepo, repo)() }
            fail("Debe lanzar SinCambiosException")
        } catch (e: SinCambiosException) {
            assertFalse("No debe llamar al backend", repo.reenviarCalled)
            assertNotNull("El borrador se conserva", borradorRepo.borrador.value)
        }
    }

    @Test
    fun `structural_reenviar_con_cambio_de_texto_reenvia_sin_subir_documentos_y_limpia`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)
        borradorRepo.actualizar { b ->
            b.copy(tuHogar = b.tuHogar!!.copy(whyAdopt = "Texto corregido"))
        }

        val id = runBlocking { crearEnviar(borradorRepo, repo)() }

        assertEquals("sol-1", id)
        assertTrue(repo.reenviarCalled)
        assertEquals("sol-1", repo.reenviarId)
        assertTrue("No se subió ningún documento", repo.docsSubidos.isEmpty())
        assertNull("El borrador se limpia al terminar", borradorRepo.borrador.value)
    }

    @Test
    fun `structural_reenviar_solo_sube_el_documento_reemplazado`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)
        borradorRepo.actualizar {
            it.copy(documentos = DocumentosSolicitud(identificacion = docLocal("ine.pdf"), comprobanteDomicilio = null))
        }

        runBlocking { crearEnviar(borradorRepo, repo)() }

        assertEquals(listOf(DocumentType.INE), repo.docsSubidos)
        assertTrue(repo.reenviarCalled)
    }

    @Test
    fun `decision_condition_reenviar_con_ambos_documentos_nuevos_sube_los_dos`() {
        val repo = FakeRepo()
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)
        borradorRepo.actualizar {
            it.copy(
                documentos =
                    DocumentosSolicitud(
                        identificacion = docLocal("ine.pdf"),
                        comprobanteDomicilio = docLocal("dom.pdf"),
                    ),
            )
        }

        runBlocking { crearEnviar(borradorRepo, repo)() }

        assertEquals(listOf(DocumentType.INE, DocumentType.DOMICILIO), repo.docsSubidos)
    }

    @Test
    fun `void_value_reenviar_con_error_del_servidor_conserva_el_borrador`() {
        val repo = FakeRepo().apply { errorAlReenviar = NoEditableException("ya no") }
        val borradorRepo = BorradorRepositoryImpl()
        prepararEdicion(repo, borradorRepo)
        borradorRepo.actualizar { b -> b.copy(tuHogar = b.tuHogar!!.copy(whyAdopt = "Otro texto")) }

        try {
            runBlocking { crearEnviar(borradorRepo, repo)() }
            fail("Debe propagar el error")
        } catch (e: NoEditableException) {
            assertNotNull(borradorRepo.borrador.value)
        }
    }

    // ============================================================================
    // SECCIÓN 5: Mapper SolicitudEdicionDto.toDomain() (range / boundary)
    // ============================================================================
    @Test
    fun `range_mapper_tipo_vivienda_se_convierte_al_enum`() {
        assertEquals(
            HouseType.CASA,
            dtoBase()
                .copy(tipoVivienda = "Casa")
                .toDomain()
                .tuHogar
                ?.houseType,
        )
        assertEquals(
            HouseType.DEPARTAMENTO,
            dtoBase()
                .copy(tipoVivienda = "Depto")
                .toDomain()
                .tuHogar
                ?.houseType,
        )
        assertEquals(
            HouseType.DEPARTAMENTO,
            dtoBase()
                .copy(tipoVivienda = "Departamento")
                .toDomain()
                .tuHogar
                ?.houseType,
        )
        assertEquals(
            HouseType.OTRO,
            dtoBase()
                .copy(tipoVivienda = "Otro")
                .toDomain()
                .tuHogar
                ?.houseType,
        )
    }

    @Test
    fun `boundary_mapper_valores_nulos_usan_defaults_seguros`() {
        val r = dtoBase().copy(tipoVivienda = null, personasEnCasa = null).toDomain()

        assertEquals("Sin tipo → CASA", HouseType.CASA, r.tuHogar?.houseType)
        assertEquals("Sin personas → 1", 1, r.tuHogar?.peopleInHouse)
    }

    @Test
    fun `structural_mapper_copia_flags_de_documentos_e_ids`() {
        val r = dtoBase().toDomain()

        assertEquals("sol-1", r.idSolicitud)
        assertEquals("dog1", r.idPerro)
        assertEquals("Ingeniera", r.ocupacion)
        assertTrue(r.tieneIne)
        assertFalse(r.tieneDomicilio)
    }

    // ============================================================================
    // SECCIÓN 6: MisSolicitudesViewModel.onEditar (estados de la UI)
    // ============================================================================
    private fun crearVm(repo: FakeRepo) =
        MisSolicitudesViewModel(
            GetMisSolicitudesUseCase(repo),
            IniciarEdicionUseCase(repo, BorradorRepositoryImpl()),
        )

    @Test
    fun `structural_onEditar_exitoso_termina_en_Lista`() {
        val repo = FakeRepo().apply { paraEditar = solicitudParaEditar() }
        val vm = crearVm(repo)

        vm.onEditar("user1", "sol-1")

        assertEquals(EdicionUiState.Lista, vm.edicionState.value)
    }

    @Test
    fun `decision_condition_onEditar_no_editable_termina_en_NoEditable`() {
        val repo = FakeRepo().apply { errorAlEditar = NoEditableException("x") }
        val vm = crearVm(repo)

        vm.onEditar("user1", "sol-1")

        assertTrue(vm.edicionState.value is EdicionUiState.NoEditable)
    }

    @Test
    fun `decision_condition_onEditar_con_error_de_red_termina_en_Error`() {
        val repo = FakeRepo().apply { errorAlEditar = java.io.IOException("sin red") }
        val vm = crearVm(repo)

        vm.onEditar("user1", "sol-1")

        assertTrue(vm.edicionState.value is EdicionUiState.Error)
    }

    @Test
    fun `void_value_onEdicionAtendida_regresa_a_Idle`() {
        val repo = FakeRepo().apply { paraEditar = solicitudParaEditar() }
        val vm = crearVm(repo)
        vm.onEditar("user1", "sol-1")

        vm.onEdicionAtendida()

        assertEquals(EdicionUiState.Idle, vm.edicionState.value)
    }
}
