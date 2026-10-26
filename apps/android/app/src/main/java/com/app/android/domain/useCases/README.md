# ⚙️ Domain / UseCases

### 📍 ¿En qué parte del MVVM + Clean me encuentro?

Estás en la capa **Domain (El Corazón de la App)**. Esta subcarpeta contiene los **Casos de Uso** (Reglas de Negocio concretas).

### 🎯 Objetivo y Tarea

Resolver una sola acción específica de negocio (ej. `GetDogByIdUseCase`, `SaveScoreUseCase`).

- **Regla estricta:** Recibe la `RepoInterface` en su constructor con `@Inject`. Aplica validaciones o filtros de negocio y se los entrega al ViewModel.

### 📝 Plantilla Universal de Código

```kotlin
package com.tuempresa.app.domain.useCases

import com.tuempresa.app.domain.entities.NombreEntidad
import com.tuempresa.app.domain.repoInterface.NombreRepositorio
import javax.inject.Inject

class ObtenNombreCasoDeUso @Inject constructor(
    private val repository: NombreRepositorio
) {
    suspend operator fun invoke(id: String): NombreEntidad {
        // Validaciones o reglas de negocio aquí
        return repository.obtenerPorId(id)
    }
}
```
