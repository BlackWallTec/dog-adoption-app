package com.app.android.data.repoImp.repoImpVoluntariado

import android.util.Log
import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.dtos.dtosVoluntariado.ProgramaDto
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import javax.inject.Inject

// Mapper local DTO -> Entity
// HU39A solo trae 6 campos; los demás de la entidad compartida quedan sin dato.
fun ProgramaDto.toDomain() =
    Programa(
        id = idPrograma,
        nombre = nombre,
        tipo = tipo,
        descripcion = null,
        fechaInicio = fechaInicio.aInstant(),
        fechaFin = fechaFin.aInstant(),
        ubicacion = null,
        cupoMaximo = null,
        cupoDisponible = null,
        estado = EstadoPrograma.desdeTexto(estadoDelPrograma),
        usuarioInscrito = usuarioInscrito == true,
    )

class ProgramaRepositoryImpl @Inject constructor(
    private val api: ProgramaApiService
) : ProgramaRepository {

    override suspend fun obtenerProgramasVigentes(): List<Programa> {
        // Un programa con datos inválidos se omite y se registra, sin afectar a los demás.
        return api.obtenerProgramas().data.mapNotNull { dto ->
            runCatching { dto.toDomain() }
                .onFailure { Log.e("HU39A", "Programa con datos inválidos: ${dto.idPrograma}", it) }
                .getOrNull()
        }
    }
}