package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaApiService
import com.app.android.data.dataSource.dataSourcesVoluntariado.ProgramaDto
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa
import com.app.android.domain.repoInterface.repoIntVoluntariado.ProgramaRepository
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

// Las fechas de la BD no llevan zona: se interpretan en la zona del refugio, igual que InscripcionMapper.
private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")

private fun String.aInstant(): Instant = LocalDateTime.parse(this).atZone(ZONA_REFUGIO).toInstant()

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
        usuarioInscrito = false,
    )

class ProgramaRepositoryImpl @Inject constructor(
    private val api: ProgramaApiService
) : ProgramaRepository {

    override suspend fun obtenerProgramasVigentes(): List<Programa> {
        return api.obtenerProgramas().data.map { it.toDomain() }
    }
}