package com.app.android.domain.entities.entitiesSolicitudes

/*
Pendientes a añadir en la BD:
alter table public.solicitud_adopcion
  drop constraint if exists compromiso_textos_max_1000;

alter table public.solicitud_adopcion
  add constraint compromiso_textos_max_300 check (
    char_length(cuidados_basicos) <= 300
    and char_length(opinion_esterilizacion) <= 300
    and char_length(detalle_veterinario) <= 300
    and char_length(tiempo_solo) <= 300
    and char_length(rutina_diaria) <= 300
    and char_length(manejo_comportamiento) <= 300
  ) not valid;
 */

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
)
