package com.app.android.domain.repoInterface.repoIntCasesCatalogo
import com.app.android.domain.entities.entitiesCatalogo.Perro
interface PerroRepository{
    suspend fun getPerros(): List<Perro>
}