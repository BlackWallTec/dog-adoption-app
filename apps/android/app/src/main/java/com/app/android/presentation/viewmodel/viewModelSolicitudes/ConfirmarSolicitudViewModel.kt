package com.app.android.presentation.viewmodel.viewModelSolicitudes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.data.repoImp.repoImpSolicitudes.SolicitudHttpException
import com.app.android.domain.entities.entitiesSolicitudes.HouseType
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import com.app.android.domain.useCases.useCasesCatalogo.GetDogByIdUseCase
import com.app.android.domain.useCases.useCasesSolicitudes.EnviarSolicitudUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConfirmarSolicitudViewModel
    @Inject
    constructor(
        private val borradorRepo: BorradorRepositoryInt,
        private val getDogByIdUseCase: GetDogByIdUseCase,
        private val enviarSolicitudUseCase: EnviarSolicitudUseCase,
    ) : ViewModel() {
        @Suppress("ktlint:standard:backing-property-naming")
        private val _enviando = MutableStateFlow(false)
        private val _errorMessage = MutableStateFlow<String?>(null)
        val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

        @Suppress("ktlint:standard:backing-property-naming")
        private val _mascota =
            MutableStateFlow<MascotaResumen>(
                MascotaResumen(nombre = "Cargando...", detalle = "", raza = ""),
            )

        init {
            cargarMascota()
        }

        private fun cargarMascota() {
            viewModelScope.launch {
                val idPerro = borradorRepo.borrador.value?.idPerro
                if (idPerro != null) {
                    try {
                        val perro = getDogByIdUseCase(idPerro)
                        if (perro != null) {
                            _mascota.value =
                                MascotaResumen(
                                    nombre = perro.nombre,
                                    detalle = "${perro.edad ?: 0} años · ${perro.sexo}",
                                    raza = "${perro.razaNombre} · Querétaro",
                                    fotoUrl = perro.multimedia.firstOrNull()?.url,
                                )
                        } else {
                            _mascota.value = MascotaResumen("Mascota seleccionada", "", "")
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        _mascota.value = MascotaResumen("Mascota seleccionada", "", "")
                    }
                } else {
                    _mascota.value = MascotaResumen("Mascota seleccionada", "", "")
                }
            }
        }

        val uiState: StateFlow<ConfirmarSolicitudUiState> =
            combine(
                borradorRepo.borrador.filterNotNull(),
                _mascota,
                _enviando,
            ) { borrador, mascota, enviando ->
                val dg = borrador.datosGenerales
                val hogar = borrador.tuHogar
                val comp = borrador.compromiso

                val hasPets = hogar?.hadPet == true || hogar?.otherPets == true

                val secciones =
                    listOf(
                        SeccionResumen(
                            tipo = SeccionSolicitud.DATOS_GENERALES,
                            titulo = "Datos generales",
                            campos =
                                listOf(
                                    CampoResumen("Ocupación", dg?.ocupacion?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                ),
                        ),
                        SeccionResumen(
                            tipo = SeccionSolicitud.TU_HOGAR,
                            titulo = "Tu hogar",
                            campos =
                                listOf(
                                    CampoResumen(
                                        "¿Has tenido mascotas antes?",
                                        hogar?.hadPet?.let { if (it) "Sí" else "No" } ?: "Pendiente",
                                    ),
                                    CampoResumen(
                                        "¿Tienes otros animales?",
                                        hogar?.otherPets?.let { if (it) "Sí" else "No" } ?: "Pendiente",
                                    ),
                                    CampoResumen(
                                        "Sobre tus mascotas",
                                        if (hasPets) (hogar.aboutPets.takeIf { it.isNotBlank() } ?: "Pendiente") else "No aplica",
                                    ),
                                    CampoResumen("¿Por qué deseas adoptar?", hogar?.whyAdopt?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("¿Dónde vivirá y dormirá?", hogar?.whereLive?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("Personas en casa", hogar?.peopleInHouse?.toString()?.takeIf { it != "0" } ?: "Pendiente"),
                                    CampoResumen(
                                        "¿Todos están de acuerdo?",
                                        hogar?.allAgree?.let { if (it) "Sí" else "No" } ?: "Pendiente",
                                    ),
                                    CampoResumen(
                                        "Casa o departamento",
                                        when (hogar?.houseType) {
                                            HouseType.CASA -> "Casa"
                                            HouseType.DEPARTAMENTO -> "Departamento"
                                            HouseType.OTRO -> "Otro"
                                            null -> "Pendiente"
                                        },
                                    ),
                                    CampoResumen("Jardín o patio", hogar?.hasGarden?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen(
                                        "¿Casero acepta mascotas?",
                                        hogar?.petAccept?.let { if (it) "Sí" else "No" } ?: "No aplica",
                                    ),
                                ),
                        ),
                        SeccionResumen(
                            tipo = SeccionSolicitud.COMPROMISO,
                            titulo = "Compromiso",
                            campos =
                                listOf(
                                    CampoResumen("Cuidados básicos", comp?.petNeeds?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("Esterilización", comp?.petSterilization?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("Veterinario", comp?.hasVet?.let { if (it) "Sí" else "No" } ?: "Pendiente"),
                                    CampoResumen("Detalle veterinario", comp?.vetDetail?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("Tiempo solo", comp?.timeAlone?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen("Rutina diaria", comp?.dailyRoutine?.takeIf { it.isNotBlank() } ?: "Pendiente"),
                                    CampoResumen(
                                        "Manejo de comportamiento",
                                        comp?.dogManagement?.takeIf { it.isNotBlank() } ?: "Pendiente",
                                    ),
                                ),
                        ),
                    )

                val docs =
                    listOfNotNull(
                        borrador.documentos?.identificacion?.nombre,
                        borrador.documentos?.comprobanteDomicilio?.nombre,
                    )

                ConfirmarSolicitudUiState(
                    mascota = mascota,
                    secciones = secciones,
                    documentos = docs,
                    enviando = enviando,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue =
                    ConfirmarSolicitudUiState(
                        mascota = MascotaResumen("Mascota seleccionada", "", ""),
                        secciones = emptyList(),
                        documentos = emptyList(),
                        enviando = false,
                    ),
            )

        fun enviarSolicitud(
            onSuccess: (String) -> Unit,
            onUnauthorized: () -> Unit,
        ) {
            if (_enviando.value) return

            _enviando.value = true
            _errorMessage.value = null

            viewModelScope.launch {
                try {
                    val borrador = borradorRepo.borrador.value ?: return@launch
                    val estadoActual = uiState.value

                    val snapshot =
                        SolicitudEnviadaSnapshot(
                            solicitudId = "",
                            mascota = estadoActual.mascota,
                        )

                    val solicitudId = enviarSolicitudUseCase()

                    borradorRepo.guardarConfirmacionEnviada(
                        snapshot.copy(solicitudId = solicitudId),
                    )

                    onSuccess(solicitudId)
                } catch (e: Exception) {
                    if (e is CancellationException) throw e
                    val statusCode = (e as? SolicitudHttpException)?.statusCode
                    val msg =
                        when (statusCode) {
                            400 -> {
                                e.message.ifBlank { "Datos inválidos en la solicitud." }
                            }

                            409 -> {
                                "Ya existe una solicitud activa o borrador para este perro."
                            }

                            403 -> {
                                "Este borrador pertenece a otra cuenta de usuario."
                            }

                            401 -> {
                                onUnauthorized()
                                "Sesión expirada. Por favor inicia sesión nuevamente."
                            }

                            413 -> {
                                "La imagen es demasiado grande. Por favor recapture o reduzca la imagen."
                            }

                            else -> {
                                e.message ?: "Error al enviar la solicitud."
                            }
                        }
                    _errorMessage.value = msg
                    _enviando.value = false
                }
            }
        }

        fun onErrorShown() {
            _errorMessage.value = null
        }
    }
