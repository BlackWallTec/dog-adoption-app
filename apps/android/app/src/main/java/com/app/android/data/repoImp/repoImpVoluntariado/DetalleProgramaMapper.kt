package com.app.android.data.repoImp.repoImpVoluntariado

import com.app.android.data.dtos.dtosVoluntariado.DetalleProgramaDto
import com.app.android.domain.entities.entitiesVoluntariado.DetallePrograma
import com.app.android.domain.entities.entitiesVoluntariado.EstadoPrograma
import com.app.android.domain.entities.entitiesVoluntariado.Programa

// HU36 DTO del detalle a entidad. El cupo disponible se calcula con los inscritos
internal fun DetalleProgramaDto.toDomain(): DetallePrograma {
    val totalInscritos = inscritos ?: 0
    return DetallePrograma(
        programa =
            Programa(
                id = idPrograma,
                nombre = nombre,
                tipo = tipo,
                descripcion = descripcion,
                fechaInicio = fechaInicio.aInstant(),
                fechaFin = fechaFin.aInstant(),
                ubicacion = ubicacion,
                cupoMaximo = cupoMaximo,
                cupoDisponible = cupoMaximo?.let { maxOf(it - totalInscritos, 0) },
                estado = EstadoPrograma.desdeTexto(estadoDelPrograma),
                usuarioInscrito = usuarioInscrito == true,
            ),
        tipoDescripcion = tipoDescripcion?.takeIf { it.isNotBlank() },
        inscritos = totalInscritos,
        estadoGuardado = estadoDelPrograma?.trim(),
    )
}
