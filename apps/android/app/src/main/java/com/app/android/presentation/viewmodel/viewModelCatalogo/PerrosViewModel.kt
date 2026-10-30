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


@HiltViewModel
class PerrosViewModel @Inject constructor(
    private val getPerros: GetPerrosUseCase
): ViewModel(){
    var perros by mutableStateOf<List<Perro>>(emptyList())
        private set
    init {
        viewModelScope.launch{
            try{
                perros=getPerros()
            } catch(e: Exception){      //
                Log.e("Perros", "Error: ${e.message}") //
            }
        }
    }
}