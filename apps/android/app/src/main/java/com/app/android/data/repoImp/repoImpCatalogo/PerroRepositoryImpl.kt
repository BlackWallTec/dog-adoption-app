package com.app.android.data.repoImp.repoImpCatalogo

import com.app.android.data.dataSource.dataSourcesCatalogo.PerroDataSource
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository

class PerroRepositoryImpl(
    private val dataSource: PerroDataSource
): PerroRepository {
    override suspend fun getPerros(): List<Perro>= dataSoruce.getPerros().map { Perro(it.nombre, it.edad, it.foto_url) }
}
