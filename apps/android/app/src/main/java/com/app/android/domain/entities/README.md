# 📦 Domain / Entities

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Domain (El Corazón de la App)**. Esta subcarpeta contiene la definición pura de las **Entidades de Negocio**.

### 🎯 Objetivo y Tarea

Definir las estructuras de datos ideales de tu app en Kotlin puro.

- **Regla estricta:** NO agregar anotaciones de bases de datos (`@Entity` de Room), ni de red (`@SerializedName` de Gson/Retrofit), ni librerías de Android.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.domain.entities

data class NombreEntidad(
    val id: String,
    val campoTexto: String,
    val campoNumero: Int,
    val esActivo: Boolean
)
```
