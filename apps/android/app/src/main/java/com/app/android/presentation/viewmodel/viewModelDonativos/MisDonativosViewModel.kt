package com.app.android.presentation.viewmodel.viewModelDonativos
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesDonativos.Donativo
import com.app.android.domain.useCases.useCasesDonativos.GetMisDonativosUseCase
import com.app.android.domain.useCases.useCasesDonativos.SolicitarCfdiUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MisDonativosUiState(
    val cargando: Boolean = true,
    val donativos: List<Donativo> = emptyList(),
    val error: String? = null,
    val enviandoCfdi: String? = null, // id del donativo que se está mandando
    val mensaje: String? = null,
)

@HiltViewModel
class MisDonativosViewModel
@Inject
constructor(
    private val getMisDonativosUseCase: GetMisDonativosUseCase,
    private val solicitarCfdiUseCase: SolicitarCfdiUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MisDonativosUiState())
    val uiState: StateFlow<MisDonativosUiState> = _uiState

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            try {
                val lista = getMisDonativosUseCase()
                _uiState.update { it.copy(cargando = false, donativos = lista) }
            } catch (e: Exception) {
                Log.e("DONATIVOS", "Error al cargar mis donativos: ${e.message}", e)
                _uiState.update { it.copy(cargando = false, error = "No pudimos cargar tus donativos.") }
            }
        }
    }

    // HU49 CA-01: pedir el CFDI de un donativo validado
    fun onPedirCfdi(idDonativo: String) {
        // para que dos toques seguidos no manden dos solicitudes
        if (_uiState.value.enviandoCfdi != null) return

        viewModelScope.launch {
            _uiState.update { it.copy(enviandoCfdi = idDonativo, mensaje = null) }
            try {
                solicitarCfdiUseCase(idDonativo)
                _uiState.update { estado ->
                    estado.copy(
                        enviandoCfdi = null,
                        mensaje = "Listo, pedimos tu CFDI. El refugio lo va a revisar.",
                        donativos =
                            estado.donativos.map {
                                if (it.id == idDonativo) it.copy(cfdiSolicitado = true) else it
                            },
                    )
                }
            } catch (e: Exception) {
                Log.e("DONATIVOS", "Error al pedir el CFDI: ${e.message}", e)
                _uiState.update {
                    it.copy(enviandoCfdi = null, mensaje = "No se pudo pedir tu CFDI. Intenta de nuevo.")
                }
            }
        }
    }
}