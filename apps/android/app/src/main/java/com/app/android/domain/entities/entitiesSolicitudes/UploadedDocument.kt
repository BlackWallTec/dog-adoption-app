package com.app.android.domain.entities.entitiesSolicitudes

//Uploaded document is the document that the system is going to recieve
//it represents the file after it has been succesfully uploadeds
data class UploadedDocument(
    //type of document
    val type: DocumentType,
    //name of file
    val fileName: String? = null,
    //size of file
    val sizeBytes: Long
)