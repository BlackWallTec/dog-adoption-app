package com.app.android.data.repoImp.repoImpCatalogo

import com.app.android.data.dataSource.dataSourcesCatalogo.PerroDataSource
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository
import jakarta.inject.Inject

class PerroRepositoryImpl @Inject constructor(
    private val dataSource: PerroDataSource
): PerroRepository {
    override suspend fun getPerros():  List<Perro> =     //
        dataSource.getPerros().map { dto ->   //
            Perro(
                nombre= dto.nombre,
                edad=dto.edad,
                fotoUrl= dto.foto_url ?: ""         ///
            )
        }
}