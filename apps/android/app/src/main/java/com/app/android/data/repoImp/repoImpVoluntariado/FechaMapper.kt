package com.app.android.data.repoImp.repoImpVoluntariado

import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId

private val ZONA_REFUGIO: ZoneId = ZoneId.of("America/Mexico_City")

// La fecha puede llegar con zona ("...Z", "...+00:00") o sin ella; sin zona se interpreta en la del refugio.
internal fun String.aInstant(): Instant =
    runCatching { OffsetDateTime.parse(this).toInstant() }
        .getOrElse { LocalDateTime.parse(this).atZone(ZONA_REFUGIO).toInstant() }
