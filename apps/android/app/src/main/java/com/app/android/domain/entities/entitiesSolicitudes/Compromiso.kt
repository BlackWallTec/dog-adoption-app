package com.app.android.domain.entities.entitiesSolicitudes

data class Compromiso(
    // Paso 4 del Formulario | "Compromiso"
    val petNeeds: String = "", // Pregunta: Menciona los cuidados básicos de una mascota
    val petSterilization: String = "", // Pregunta: ¿Que opinas de la esterilización?
    val petFollowing: String = "", // Opción (Si, No): ¿Nos permites dar seguimiento (visitas o fotos)?
)
