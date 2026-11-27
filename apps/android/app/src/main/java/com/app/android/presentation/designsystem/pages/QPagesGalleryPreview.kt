package com.app.android.presentation.designsystem.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.app.android.domain.entities.entitiesDonativos.DatosFiscales
import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.DatosGenerales
import com.app.android.domain.entities.entitiesSolicitudes.DocumentType
import com.app.android.domain.entities.entitiesSolicitudes.EstadoSolicitud
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import com.app.android.domain.entities.entitiesVoluntariado.AccionDetalle
import com.app.android.presentation.designsystem.atoms.QStatus
import com.app.android.presentation.designsystem.atoms.QText
import com.app.android.presentation.designsystem.theme.QShapes
import com.app.android.presentation.designsystem.theme.QSpacing
import com.app.android.presentation.designsystem.theme.QTextStyle
import com.app.android.presentation.designsystem.theme.QTheme
import com.app.android.presentation.designsystem.theme.QarinoTheme
import com.app.android.presentation.view.atomicDesign.page.CampoResumen
import com.app.android.presentation.view.atomicDesign.page.ConfirmarSolicitudUiState
import com.app.android.presentation.view.atomicDesign.page.MascotaResumen
import com.app.android.presentation.view.atomicDesign.page.SeccionResumen
import com.app.android.presentation.view.atomicDesign.page.SeccionSolicitud
import com.app.android.presentation.viewmodel.viewModelSolicitudes.Acceptances
import com.app.android.presentation.viewmodel.viewModelSolicitudes.DocumentSlot
import com.app.android.presentation.viewmodel.viewModelSolicitudes.SolicitudItem
import com.app.android.presentation.viewmodel.viewModelSolicitudes.UploadDocumentsUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.FiltrosProgramasUiState
import com.app.android.presentation.viewmodel.viewModelVoluntariado.ProgramaUi

// Cada página se dibuja a tamaño de teléfono y se reduce para que quepan dos por renglón.
private val PAGE_WIDTH = 360.dp
private val PAGE_HEIGHT = 720.dp
private const val THUMBNAIL_SCALE = 0.42f
private val THUMBNAIL_WIDTH = PAGE_WIDTH * THUMBNAIL_SCALE
private val THUMBNAIL_HEIGHT = PAGE_HEIGHT * THUMBNAIL_SCALE

private val galleryPet = MascotaResumen(nombre = "Firulais", detalle = "2 años · Macho · Mediano", raza = "Mestizo · Querétaro")

private val galleryProgram =
    ProgramaUi(
        id = "1",
        tipo = "Voluntariado",
        titulo = "Paseos y socialización de fin de semana",
        fecha = "Dom. 15 nov. 2026",
        horario = "09:00 – 14:00 h",
        ubicacion = "Refugio Qariño · Querétaro",
        textoCupo = "0/25 lugares ocupados · 25 disponibles",
        progresoCupo = 0f,
        textoBoton = "Inscribirme",
        botonHabilitado = true,
    )

// (nombre de la página, vista previa con un estado de ejemplo).
private val galleryPages: List<Pair<String, @Composable () -> Unit>> =
    listOf(
        "Detalle del perro" to @Composable {
            QDetallePerroPage(
                isLoading = false,
                errorMessage = null,
                name = "Firulais",
                statusText = "Disponible",
                status = QStatus.Positive,
                location = "Refugio Principal",
                media = emptyList(),
                infoTiles = listOf("Edad" to "5 años", "Sexo" to "Macho", "Tamaño" to "Chico", "Raza" to "Mestizo"),
                onBack = {},
                onRetry = {},
                onShare = {},
                onAdoptClick = {},
                onTabSelected = {},
                healthItems = listOf("Vacunado", "Esterilizado"),
            )
        },
        "Programas" to @Composable {
            QProgramasPage(
                isLoading = false,
                errorMessage = null,
                programs = listOf(galleryProgram),
                filters = FiltrosProgramasUiState(),
                onFiltersChange = {},
                onApplyFilters = {},
                onClearFilters = {},
                onRetry = {},
                onProgramClick = {},
                onEnroll = {},
                onCancel = {},
                onMyVolunteeringClick = {},
                onTabSelected = {},
            )
        },
        "Detalle del programa" to @Composable {
            QDetalleProgramaPage(
                isLoading = false,
                errorMessage = null,
                isNotFound = false,
                type = "Paseo",
                title = "Paseos con cariño",
                description = "Acompaña a los perros del refugio en un paseo tranquilo.",
                date = "Jue. 10 dic. 2026",
                time = "09:00 – 11:00 h",
                location = "Refugio Qariño · Querétaro",
                capacityText = "8/15 lugares ocupados · 7 disponibles",
                capacityProgress = 8f / 15f,
                action = AccionDetalle.Inscribirme,
                onBack = {},
                onRetry = {},
                onEnroll = {},
                onTabSelected = {},
            )
        },
        "Mis solicitudes" to @Composable {
            QMisSolicitudesPage(
                isLoading = false,
                errorMessage = null,
                requests =
                    listOf(
                        SolicitudItem("1", "Firulais", null, "QA-0248", "12 oct 2026", EstadoSolicitud.PENDIENTE_REVISION, false, null),
                        SolicitudItem("2", "Canela", null, "QA-0231", "10 oct 2026", EstadoSolicitud.APROBADA, false, null),
                    ),
                onRetry = {},
                onEditClick = {},
                onCatalogClick = {},
                onTabSelected = {},
            )
        },
        "Requisitos (paso 1)" to @Composable {
            QRequisitosPage(
                accepted = false,
                onAcceptedChange = {},
                isLoading = false,
                errorMessage = null,
                onBack = {},
                onContinue = {},
            )
        },
        "Datos generales (paso 2)" to @Composable {
            QDatosGeneralesPage(
                isLoading = false,
                loadError = null,
                data =
                    DatosGenerales(
                        nombreCompleto = "Persona Ejemplo",
                        fechaNacimiento = "1995-03-15",
                        ocupacion = "",
                        domicilio = "Calle Ejemplo 123, Querétaro",
                        municipio = "Querétaro",
                        telefono = "442 000 0000",
                        correo = "correo@ejemplo.com",
                    ),
                age = 31,
                occupationError = null,
                onOccupationChange = {},
                isSaving = false,
                saveError = null,
                onRetry = {},
                onBack = {},
                onContinue = {},
            )
        },
        "Tu hogar (paso 3)" to @Composable {
            QTuHogarPage(
                isLoading = false,
                loadError = null,
                data = TuHogar(),
                errors = null,
                onDataChange = {},
                isSaving = false,
                saveError = null,
                onRetry = {},
                onBack = {},
                onContinue = {},
            )
        },
        "Compromiso (paso 4)" to @Composable {
            QCompromisoPage(
                isLoading = false,
                loadError = null,
                data = Compromiso(),
                errors = null,
                onDataChange = {},
                acceptances = Acceptances(),
                onAcceptancesChange = {},
                isSaving = false,
                saveError = null,
                onRetry = {},
                onBack = {},
                onContinue = {},
            )
        },
        "Documentos (paso 5)" to @Composable {
            QDocumentosPage(
                state =
                    UploadDocumentsUiState(
                        slots =
                            mapOf(
                                DocumentType.INE to DocumentSlot(fileName = "identificacion.pdf"),
                                DocumentType.DOMICILIO to DocumentSlot(),
                            ),
                    ),
                onPickFile = {},
                onBack = {},
                onContinue = {},
            )
        },
        "Confirmar (paso 6)" to @Composable {
            QConfirmarSolicitudPage(
                state =
                    ConfirmarSolicitudUiState(
                        mascota = galleryPet,
                        secciones =
                            listOf(
                                SeccionResumen(
                                    tipo = SeccionSolicitud.DATOS_GENERALES,
                                    titulo = "Datos generales",
                                    campos = listOf(CampoResumen("Nombre completo", "Persona Ejemplo")),
                                ),
                            ),
                        documentos = listOf("identificacion.pdf"),
                    ),
                onBack = {},
                onEditSection = {},
                onSubmit = {},
            )
        },
        "Solicitud enviada" to @Composable {
            QSolicitudEnviadaPage(
                requestId = "QA-9AAC227E",
                pet = galleryPet,
                onBack = {},
                onMyRequestsClick = {},
                onCatalogClick = {},
                onTabSelected = {},
            )
        },
        "Donar dinero" to @Composable {
            QDonarDineroPage(
                bankData = listOf("Banco" to "[BANCO]", "Titular" to "[TITULAR]", "CLABE" to "[CLABE]"),
                receiptFileName = null,
                onPickReceipt = {},
                onCopy = { _, _ -> },
                onSubmit = { _, _ -> },
                onMyDonationsClick = {},
                onTabSelected = {},
            )
        },
        "Donativo enviado" to @Composable {
            QDonativoEnviadoPage(
                amount = "$500.00 MXN",
                onBack = {},
                onDonateAgainClick = {},
                onMyDonationsClick = {},
                onTabSelected = {},
            )
        },
        "Datos fiscales" to @Composable {
            QDatosFiscalesPage(
                isLoading = false,
                loadError = null,
                data = DatosFiscales(rfc = "XAXX010101000", regimenFiscal = "626"),
                hasSavedData = false,
                errors = null,
                onRfcChange = {},
                onBusinessNameChange = {},
                onTaxRegimeChange = {},
                onPostalCodeChange = {},
                onBillingEmailChange = {},
                onPickCertificate = {},
                onSave = {},
                onRetry = {},
                onBack = {},
                onTabSelected = {},
            )
        },
        "Mis donativos" to @Composable {
            QMisDonativosPage(
                isLoading = false,
                errorMessage = null,
                donations =
                    listOf(
                        Donativo(id = "1", monto = 500.0, fecha = "2026-11-20", estado = "Validado", cfdiSolicitado = false),
                        Donativo(id = "2", monto = 200.0, fecha = "2026-10-15", estado = "Pendiente", cfdiSolicitado = false),
                    ),
                onBack = {},
                onRetry = {},
                onRequestCfdi = {},
                onTabSelected = {},
            )
        },
        "Configuración" to @Composable {
            QConfiguracionPage(
                onMyAccountClick = {},
                onMyRequestsClick = {},
                onMyProgramsClick = {},
                onFiscalDataClick = {},
                onTabSelected = {},
            )
        },
    )

// Miniatura: la página a tamaño real, reducida y recortada, con su nombre debajo.
@Composable
private fun PageThumbnail(
    label: String,
    page: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.width(THUMBNAIL_WIDTH),
        verticalArrangement = Arrangement.spacedBy(QSpacing.xs),
    ) {
        Box(
            modifier =
                Modifier
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .clip(QShapes.small),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier =
                    Modifier
                        .requiredSize(PAGE_WIDTH, PAGE_HEIGHT)
                        .scale(THUMBNAIL_SCALE),
            ) {
                page()
            }
        }
        QText(text = label, style = QTextStyle.Caption, maxLines = 1)
    }
}

/**
 * Galería de páginas: una miniatura por página con un estado de ejemplo. Catálogo, Mis voluntariados y
 * Confirmación de inscripción no se incluyen (tienen sheet o diálogo); tienen su propio preview.
 *
 * Dueño: P5 (Páginas).
 */
@Preview(showBackground = true, widthDp = 360, heightDp = 3200, name = "Galería de páginas")
@Composable
private fun QPagesGalleryPreview() {
    QarinoTheme {
        Column(
            modifier =
                Modifier
                    .fillMaxSize()
                    .background(QTheme.colors.backgroundPlain)
                    .padding(QSpacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(QSpacing.m),
        ) {
            QText(text = "Galería de páginas", style = QTextStyle.Title)
            galleryPages.chunked(2).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(QSpacing.m)) {
                    row.forEach { (label, page) -> PageThumbnail(label = label, page = page) }
                }
            }
        }
    }
}
