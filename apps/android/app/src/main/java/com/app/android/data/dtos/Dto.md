# 📦 Data / DTOs

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Data (Modelos de Transporte)**. Esta subcarpeta (`data/dtos`) contiene los **DTOs** (Data Transfer Objects): la forma exacta en que los datos viajan desde y hacia la API o la BD.

### 🎯 Objetivo y Tarea

Representar el JSON (o la fila de BD) **tal como lo manda el backend**, y convertirlo a una Entity de domain para que el resto de la app nunca dependa del formato externo.

- **Ubicación:** los DTOs viven en `data/dtos`, **no** en `data/dataSource`. El `ApiService` (dataSource) y el `RepositoryImpl` (repoImp) los importan desde aquí.
- **Regla estricta:** Aquí viven las anotaciones de serialización (`@SerializedName`, `@Json`, `@Serializable`). **Nunca** van en domain.
- **Campos nulables:** el backend puede mandar `null` o no mandar el campo. El DTO lo refleja con `?`; el mapper decide el valor por defecto.
- **Un mapper por DTO, junto al DTO:** cada DTO trae su función `toEntity()` (DTO → Entity) y, si la app envía datos, su `toRequestDto()` (Entity → DTO). Los mappers **no** se definen dentro del repositorio, para que cualquier repositorio los reutilice.
- **Nombres fijos en todo el proyecto:** `toEntity()` y `toRequestDto()`. No uses `toDomain()` ni `toDto()`.
- **El DTO no sale de Data:** el repositorio siempre devuelve **Entities**, nunca DTOs.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.data.dtos

import com.google.gson.annotations.SerializedName
import com.tuempresa.app.domain.entities.NombreEntidad

// 1. DTO de respuesta (lo que llega de la API)
data class NombreDto(
    @SerializedName("id_nombre") val id: String?,
    @SerializedName("campo_texto") val campoTexto: String?,
    @SerializedName("campo_numero") val campoNumero: Int?,
    @SerializedName("es_activo") val esActivo: Boolean?
)

// 2. Mapper DTO -> Entity (valores por defecto para los nulos)
fun NombreDto.toEntity(): NombreEntidad = NombreEntidad(
    id = id.orEmpty(),
    campoTexto = campoTexto.orEmpty(),
    campoNumero = campoNumero ?: 0,
    esActivo = esActivo ?: false
)

// 3. DTO de petición (lo que se envía a la API)
data class NombreRequestDto(
    @SerializedName("campo_texto") val campoTexto: String,
    @SerializedName("campo_numero") val campoNumero: Int
)

// 4. Mapper Entity -> DTO (para guardar / enviar)
fun NombreEntidad.toRequestDto(): NombreRequestDto = NombreRequestDto(
    campoTexto = campoTexto,
    campoNumero = campoNumero
)
```

### 🔗 ¿Dónde se usa?

```kotlin
// En NombreApiService (Data / DataSource): solo declara endpoints y DTOs
import com.tuempresa.app.data.dtos.NombreDto
import com.tuempresa.app.data.dtos.NombreRequestDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface NombreApiService {
    @GET("endpoint/{id}")
    suspend fun fetchDatos(@Path("id") id: String): NombreDto

    @GET("endpoint")
    suspend fun fetchLista(): List<NombreDto>

    @POST("endpoint")
    suspend fun guardar(@Body body: NombreRequestDto): Response<Unit>
}
```

```kotlin
// En NombreRepositorioImpl (Data / repoImp): importa el DTO y su mapper
import com.tuempresa.app.data.dtos.toEntity
import com.tuempresa.app.data.dtos.toRequestDto

override suspend fun obtenerPorId(id: String): NombreEntidad =
    api.fetchDatos(id).toEntity()   // El DTO muere aquí, sale una Entity

override suspend fun obtenerDatos(): List<NombreEntidad> =
    api.fetchLista().map { it.toEntity() }

override suspend fun guardarDato(entidad: NombreEntidad): Boolean =
    api.guardar(entidad.toRequestDto()).isSuccessful
```

### ✅ Checklist rápido

- [ ] El DTO está en `data/dtos` y el `ApiService` y el `RepositoryImpl` lo importan desde ahí.
- [ ] El DTO tiene `@SerializedName` para cada campo cuyo nombre en JSON difiere del de Kotlin.
- [ ] Los campos que el backend puede omitir son nulables, y el mapper les da un valor por defecto.
- [ ] Los mappers se llaman `toEntity()` y `toRequestDto()` y viven junto al DTO, no dentro del repositorio.
- [ ] Cada función del `RepositoryImpl` tiene su endpoint declarado en el `ApiService` (`fetchDatos`, `fetchLista`, `guardar`).
- [ ] Ningún DTO se usa fuera de la capa Data.
- [ ] La Entity de domain no tiene ninguna anotación de red.
