package com.app.android.domain.entities.entitiesSolicitudes

//this code represents the file that the user chose
//we use data clase because we are only recieving data
data class PickedFile(
    //The location of the file in the users phone
    val uri: String,
    //the name of the file
    val name: String,
    //the size of the file in bytes
    val sizeBytes: Long,
    //the type of file
    val mimeType: String
)