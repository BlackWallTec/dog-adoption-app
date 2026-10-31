# 🔌 Data / DataSource

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Data (Fuente y Conexión Externa)**. Esta subcarpeta contiene las **APIs, BD locales y DTOs**.

### 🎯 Objetivo y Tarea

Comunicarse con fuentes externas (Supabase, Retrofit, Room, DataStore) en su lenguaje nativo.

- **Regla estricta:** Aquí viven los **DTOs** (con anotaciones de red como `@SerializedName`) y las interfaces de conexión HTTP/BD.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.data.dataSource
import retrofit2.http.GET
import retrofit2.http.Path

// 2. Interface de Servicio (API / Supabase)
interface NombreApiService {
    @GET("endpoint/{id}")
    suspend fun fetchDatos(@Path("id") id: String): NombreDto
}
```
