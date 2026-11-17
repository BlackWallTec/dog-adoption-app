package com.app.android.domain.entities.entitiesCatalogo

class ShareContent (
    val nombre: String,
    val edad: Int?,
    val sexo: String,
    val descripcion: String,
    val enlace: String,
){
    fun texto(): String=
        "Conoce a $nombre! \n"+
        "Edad: $edad \n"+
        "Sexo: $sexo \n"+
        "Descripcion: $descripcion \n"+
        "Ádoptalo aqui: $enlace"
}