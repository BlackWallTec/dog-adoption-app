package com.app.android.domain.entities.entitiesSolicitudes

/*
Pendientes a añadir en la BD:
alter table public.solicitud_adopcion
  add column if not exists tiene_veterinario boolean,
  add column if not exists detalle_veterinario character varying,
  add column if not exists tiempo_solo character varying,
  add column if not exists rutina_diaria character varying,
  add column if not exists manejo_comportamiento character varying;
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
