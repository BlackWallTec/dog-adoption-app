package com.app.android.domain.entities.entitiesVoluntariado

sealed interface Resultado<out T> {
    data class Exito<T>(val data: T) : Resultado<T>

    data class Error(val error: VoluntariadoError) : Resultado<Nothing>
}