package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaDto
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import javax.inject.Inject

// Mapper local DTO -> Entity
fun ProgramaDto.toDomain() = Programa(
    id = idPrograma,
    tipo = tipo,
    nombre = nombre,
    estadoDelPrograma = estadoDelPrograma ?: "Disponible",
    fechaInicio = fechaInicio,
    fechaFin = fechaFin
)

class ProgramaRepositoryImpl @Inject constructor(
    private val api: ProgramaApiService
) : ProgramaRepository {

    override suspend fun obtenerProgramasVigentes(): List<Programa> {
        return api.obtenerProgramas().data.map { it.toDomain() }
    }
}