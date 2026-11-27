package com.app.android.data.repoImp.repoImpCatalogo

// import com.app.android.data.dataSource.dataSourcesCatalogo.PerroDataSource
import android.util.Log
import com.app.android.data.dataSource.dataSourcesCatalogo.DogApiService
import com.app.android.domain.entities.entitiesCatalogo.Perro
import com.app.android.domain.repoInterface.repoIntCasesCatalogo.PerroRepository
import jakarta.inject.Inject

class PerroRepositoryImpl
    @Inject
    constructor(
        private val apiService: DogApiService,
    ) : PerroRepository {
        override suspend fun getPerros(): List<Perro> {
            // 1. Obtener la respuesta y la guardas en una variable
            val response = apiService.getPerros()

            // 2. Imprimir el log en el Logcat
            Log.d("DOG_DEBUG", "Perros obtenidos de la API: ${response.data}")

            // 3. Mapeas y retornas el resultado
            return response.data.map { dto ->
                Perro(
                    id = dto.id,
                    nombre = dto.nombre,
                    edad = dto.edad,
                    sexo = dto.sexo,
                    descripcion = dto.descripcion ?: "",
                    fotoUrl = dto.foto_url ?: "",
                )
            }
        }
    }
