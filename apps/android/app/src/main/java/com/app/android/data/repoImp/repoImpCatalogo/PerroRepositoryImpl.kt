package com.app.android.data.repoImp.repoImpCatalogo

//import com.app.android.data.dataSource.dataSourcesCatalogo.PerroDataSource
import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository
import jakarta.inject.Inject

class PerroRepositoryImpl @Inject constructor(
    private val apiService: DogApiService
): PerroRepository {
    override suspend fun getPerros():  List<Perro> =     //
        apiService.getPerros().data.map { dto ->   //
            Perro(
                id=dto.id, //nuevo
                nombre= dto.nombre,
                edad=dto.edad,
                fotoUrl= dto.foto_url ?: ""         ///
            )
        }
}