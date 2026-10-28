package com.app.android.presentation.viewmodel.viewModelCatalogo

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.android.data.dataSource.dataSourcesCatalogo.PerroDataSource
import com.app.android.data.repoImp.repoImpCatalogo.PerroRepositoryImpl
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.useCases.useCasesCatalogo.GetPerrosUseCase


@HiltViewModel
class PerrosViewModel: ViewModel(){
    private val getPerros= GetPerrosUseCase(PerroRepositoryImpl(PerroDataSource())
    )
    var perros by mutableStateOf<List<Perro>>(emptyList())
        private set
    init{
        viewModel.Scope.launch { ///
            try {
                perros = getPerros()
            } catch (e: Exception) {
                log.e("Perros", "Error: ${e.message}")
            }
        }
    }
}