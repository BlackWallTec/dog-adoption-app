# 🤝 Domain / RepoInterface

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Domain (El Corazón de la App)**. Esta subcarpeta contiene los **Contratos e Interfaces** de los repositorios.

### 🎯 Objetivo y Tarea

Establecer el "menú de acciones" que la aplicación puede solicitar (qué datos pedir o guardar), usando exclusivamente `Entities` de Dominio.

- **Regla estricta:** Solo declara firmas de funciones (`interface`). No lleva implementación de código ni llamadas a bases de datos/APIs.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.domain.repoInterface

import com.tuempresa.app.domain.entities.NombreEntidad

interface NombreRepositorio {
    suspend fun obtenerDatos(): List<NombreEntidad>
    suspend fun obtenerPorId(id: String): NombreEntidad
    suspend fun guardarDato(entidad: NombreEntidad): Boolean
}
```
