package com.app.android.presentation.viewmodel.viewModelCatalogo

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.android.domain.entities.entitiesCatalogo.Dog
import com.app.android.domain.entities.entitiesCatalogo.ShareContent
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.entities.entitiesCatalogo.ShareEvent
import com.app.android.domain.entities.entitiesCatalogo.ShareUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class CompartirViewModel @Inject constructor(): ViewModel(){
    private val _shareState= MutableStateFlow<ShareUIState>(ShareUIState.ShareMenuHidden)
    val shareState: StateFlow<ShareUIState> = _shareState.asStateFlow()
    private val _eventos= MutableSharedFlow<ShareEvent>()
    val eventos: SharedFlow<ShareEvent> = _eventos.asSharedFlow()

    fun onCompartirClick(perro: Dog){
        val content= ShareContent(
            nombre=perro.nombre,
            edad=perro.edad,
            sexo=perro.sexo,
            descripcion=perro.descripcion,
            enlace="qarino://perro/${perro.id}"
        )
        _shareState.value= ShareUIState.ShareMenu(content)
    }
    fun onFichaCompartida(){
        _shareState.value=ShareUIState.ShareMenuHidden
    }
    fun onEnlaceCopiado(){
        _shareState.value= ShareUIState.ShareMenuHidden
        viewModelScope.launch { _eventos.emit(ShareEvent.CopiadoOk)}
    }
    fun onCerrarMenuCompartir(){
        _shareState.value= ShareUIState.ShareMenuHidden
    }

}