package com.app.android.domain.useCases.useCasesDonativos

import com.app.android.domain.repoInterface.repoIntDonativos.DonativosRepositoryInt
import javax.inject.Inject

class SolicitarCfdiUseCase
@Inject
constructor(
    private val repository: DonativosRepositoryInt,
) {
    suspend operator fun invoke(idDonativo: String) = repository.solicitarCfdi(idDonativo)
}