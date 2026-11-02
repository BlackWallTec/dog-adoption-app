package com.app.android.presentation.viewmodel.viewModelCatalogo

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.useCases.useCasesCatalogo.GetPerrosUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//estados
sealed interface PerrosUiState{
    data object Loading: PerrosUiState
    data object Empty: PerrosUiState
    data class Success(
        val perros: List<Perro>
    ): PerrosUiState
    data class Error(
        val mensaje: String
    ): PerrosUiState
}

@HiltViewModel
class PerrosViewModel @Inject constructor(
    private val getPerros: GetPerrosUseCase
): ViewModel(){
    private val _uiState= MutableStateFlow<PerrosUiState>(PerrosUiState.Loading)
    val uiState: StateFlow<PerrosUiState> = _uiState
    init {
        onLoadPerros()
    }
    fun onLoadPerros(){
        viewModelScope.launch{
            _uiState.value=PerrosUiState.Loading
            try{
                val resultado=getPerros()
                if(resultado.isEmpty()) {
                    _uiState.value = PerrosUiState.Empty
                }else {
                    _uiState.value = PerrosUiState.Success(resultado)
                }
            } catch (e: Exception){
                _uiState.value=PerrosUiState.Error(e.message ?: "Error desconocido")
            }
        }
    }
}