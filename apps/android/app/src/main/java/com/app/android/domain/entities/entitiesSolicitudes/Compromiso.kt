package com.app.android.domain.entities.entitiesSolicitudes

data class Compromiso(
    // Paso 4 del Formulario | "Compromiso"
    val petNeeds: String = "", // Pregunta: Menciona los cuidados básicos de una mascota
    val petSterilization: String = "", // Pregunta: ¿Que opinas de la esterilización?
    val hasVet: Boolean? = null, // Opción (Si, No): ¿Cuéntas con un veterinario de confianza?
    val vetDetail: String = "", // Pregunta: Si tienes veterinario, ¿cuál es? Si no, ¿cómo piensas conseguir uno?
    val timeAlone: String = "", // Pregunta: ¿Cuántas horas al día estaría solo el perro y quién lo
    // cuidaría si viajas o sales de vacaciones?
    val dailyRoutine: String = "", // Pregunta: Describe un día normal para tu perro (paseos, comida, juego, dónde duerme)
    val dogManagement: String = "", // Pregunta: Si el perro ladra mucho, rompe cosas o se porta agresivo con otros perros, ¿qué harías?
    val acceptsReturn: Boolean = false, // Opción (Si, No): Si en algún momento no puedo quedarme con mi perro, me comprometo
    // a devolverlo/a al refugio y no venderlo ni abandonarlo/a."
    val petSpending: Boolean = false, // Pregunta: Entiendo que la adopción implica gastos continuos
    // (alimento, vacunas, desparasitación y consultas veterinarias).
    val petFollowing: Boolean = false, // Opción (Si, No): ¿Nos permites dar seguimiento (visitas o fotos)?
)
