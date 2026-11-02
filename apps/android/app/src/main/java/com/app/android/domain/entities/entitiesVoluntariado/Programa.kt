package com.app.android.domain.entities.entitiesVoluntariado

data class Programa(
    val id: String,
    val tipo: String,
    val nombre: String,
    val estadoDelPrograma: String,
    val fechaInicio: String,
    val fechaFin: String
)