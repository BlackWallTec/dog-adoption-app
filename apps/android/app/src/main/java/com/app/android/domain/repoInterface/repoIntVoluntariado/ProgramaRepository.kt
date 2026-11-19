package com.app.android.domain.repoInterface.repoIntVoluntariado

import com.app.android.domain.entities.entitiesVoluntariado.Programa

interface ProgramaRepository {
    suspend fun obtenerProgramasVigentes(): List<Programa>

    suspend fun obtenerMisProgramas(): List<Programa>
}