package com.app.android.presentation.viewmodel.viewModelCatalogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesCatalogo.DogFilters
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.FilterChoice
import com.app.android.domain.entities.entitiesCatalogo.FilterChoices
import com.app.android.domain.useCases.useCasesCatalogo.GetFilterChoicesUseCase
import com.app.android.domain.useCases.useCasesCatalogo.GetFilteredDogsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// filtros
@HiltViewModel
class CatalogFiltersViewModel
    @Inject
    constructor(
        private val getFilteredDogs: GetFilteredDogsUseCase,
        private val getFilterChoices: GetFilterChoicesUseCase,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow<CatalogUiState>(CatalogUiState.Loading)
        val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

        // ya aplicados
        private val _appliedFilters = MutableStateFlow(FilterSelection())
        val appliedFilters: StateFlow<FilterSelection> = _appliedFilters.asStateFlow()

        // se muestra al realizar la consulta
        private val _options = MutableStateFlow(FilterOptions())
        val options: StateFlow<FilterOptions> = _options.asStateFlow()

        // opciones con sus ids
        private var choices = FilterChoices()
        private var loadJob: Job? = null

        init {
            reload(withOptions = true)
        }

        fun onApplyFilters(selection: FilterSelection) {
            _appliedFilters.value = selection
            reload()
        }

        fun onClearFilters() {
            _appliedFilters.value = FilterSelection()
            reload()
        }

        fun onRetry() = reload(withOptions = true)

        private fun reload(withOptions: Boolean = false) {
            // si se hace una busqueda nueva, borrar la pasada
            loadJob?.cancel()
            loadJob =
                viewModelScope.launch {
                    _uiState.value = CatalogUiState.Loading

                    if (withOptions) {
                        try {
                            choices = getFilterChoices()
                            val actuales = _options.value
                            _options.value =
                                actuales.copy(
                                    tamanos = choices.tamanos.map { it.nombre }.ifEmpty { actuales.tamanos },
                                    razas = choices.razas.map { it.nombre },
                                    caracteristicas = choices.caracteristicas.map { it.nombre },
                                )
                        } catch (e: CancellationException) {
                            throw e
                        } catch (e: Exception) {
                            // Se guarda la consulta anterior
                        }
                    }

                    try {
                        val perros = getFilteredDogs(toDogFilters(_appliedFilters.value, _options.value))
                        _uiState.value =
                            if (perros.isEmpty()) {
                                CatalogUiState.Empty
                            } else {
                                CatalogUiState.Success(perros.map { it.toCardUi() })
                            }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Exception) {
                        // Los filtros aplicados se conservan para poder reintentar
                        _uiState.value =
                            CatalogUiState.Error("No pudimos cargar los perros. Revisa tu conexión e intenta de nuevo.")
                    }
                }
        }

        // pasar de seleccion a valores
        private fun toDogFilters(
            seleccion: FilterSelection,
            opciones: FilterOptions,
        ): DogFilters =
            DogFilters(
                sexo = opciones.sexos.getOrNull(seleccion.sexoIndex),
                tamanoId = idDe(choices.tamanos, opciones.tamanos.getOrNull(seleccion.tamanoIndex)),
                razaId = idDe(choices.razas, opciones.razas.getOrNull(seleccion.razaIndex)),
                edad =
                    when (seleccion.edadIndex) {
                        0 -> "cachorro"
                        1 -> "adulto"
                        2 -> "senior"
                        else -> null
                    },
                caracterIds =
                    seleccion.caracteristicas
                        .sorted()
                        .mapNotNull { idDe(choices.caracteristicas, opciones.caracteristicas.getOrNull(it)) },
            )

        private fun idDe(
            lista: List<FilterChoice>,
            nombre: String?,
        ): String? = if (nombre == null) null else lista.firstOrNull { it.nombre.equals(nombre, ignoreCase = true) }?.id

        private fun DogSummary.toCardUi(): DogCardUi =
            DogCardUi(
                id = id,
                nombre = nombre,
                edad =
                    when (edadAnios) {
                        0 -> "Menos de 1 año"
                        1 -> "1 año"
                        else -> "$edadAnios años"
                    },
                sexo = sexo,
                tamano = tamano,
                raza = raza,
                fotoUrl = fotoUrl,
            )
    }