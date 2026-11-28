package com.app.android.presentation.viewmodel.viewModelSolicitudes

import androidx.lifecycle.ViewModel
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class EnviadaViewModel
    @Inject
    constructor(
        borradorRepo: BorradorRepositoryInt,
    ) : ViewModel() {
        val confirmacionEnviada: StateFlow<SolicitudEnviadaSnapshot?> = borradorRepo.confirmacionEnviada
    }
