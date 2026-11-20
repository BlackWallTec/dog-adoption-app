package com.app.android.domain.entities.entitiesCatalogo

class ShareContent (
    val nombre: String,
    val edad: Int?,
    val sexo: String,
    val descripcion: String,
    val enlace: String,
){
    fun texto(): String= buildString {
        appendLine("Conoce a $nombre!")
        if(edad !=null){
            appendLine("Edad: $edad")
        }
        if (sexo.isNotBlank()){
            appendLine("Sexo: $sexo")
        }
        if (descripcion.isNotBlank()) {
            appendLine("Descripción: $descripcion")
        }
        appendLine("Adoptalo aqui: $enlace")
    }
}