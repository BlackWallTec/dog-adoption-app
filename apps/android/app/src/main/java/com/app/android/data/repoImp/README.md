# 🛠️ Data / RepoImp

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Data (Coordinador y Mapeador)**. Esta subcarpeta contiene las **Implementaciones Concretas** de las interfaces de Dominio.

### 🎯 Objetivo y Tarea

Coordinar las llamadas al `DataSource`, recibir los `DTOs`, **mapearlos a Entities de Dominio** y retornar los datos limpios a la capa Domain.

- **Regla estricta:** Debe implementar (`: NombreRepositorio`) el contrato de `Domain` e inyectar sus fuentes de datos con `@Inject`.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.data.repoImp

import com.tuempresa.app.data.dataSource.NombreApiService
import com.tuempresa.app.data.dataSource.NombreDto
import com.tuempresa.app.domain.entities.NombreEntidad
import com.tuempresa.app.domain.repoInterface.NombreRepositorio
import javax.inject.Inject

// Mapper local DTO -> Entity
fun NombreDto.toDomain() = NombreEntidad(id = id, campoTexto = nombre, campoNumero = 0, esActivo = true)

class NombreRepositorioImpl @Inject constructor(
    private val api: NombreApiService
) : NombreRepositorio {

    override suspend fun obtenerPorId(id: String): NombreEntidad {
        val dto = api.fetchDatos(id)
        return dto.toDomain()
    }

    override suspend fun obtenerDatos(): List<NombreEntidad> TODO()
    override suspend fun guardarDato(entidad: NombreEntidad): Boolean TODO()
}

```
