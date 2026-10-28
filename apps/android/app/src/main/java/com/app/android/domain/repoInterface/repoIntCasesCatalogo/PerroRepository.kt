package com.app.android.domain.repoInterface.repoIntCasesCatalogo

interface PerroRepository{
    suspend fun getPerros(): List<Perro>
}