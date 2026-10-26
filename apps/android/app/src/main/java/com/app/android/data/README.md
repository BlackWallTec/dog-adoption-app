# 💾 Capa de Datos (Data Layer)

> **El Almacén, Proveedor de Red y Traductor**

---

## 📌 1. Definición, Objetivo y Fundamentos

La capa de **Data** es la encargada de la infraestructura, comunicación externa y persistencia[cite: 3, 4]. Su único objetivo es conseguir los datos crudos desde internet (APIs/Supabase) o la base de datos local (Room/SQLite), **transformarlos** y entregarlos limpios a la capa de `Domain`[cite: 3, 4].

### Fundamentos Clave:

- **Encapsulación de Fuentes:** Oculta al resto de la aplicación el origen real de los datos. El resto de la app no sabe si un dato vino de una caché local o de una petición HTTP[cite: 4].
- **Traducción de Modelos (Mappers):** Recibe datos "sucios" o nativos del servidor en forma de **DTOs** (`Data Transfer Objects`) y los convierte mediante Mappers a **Entities** de Dominio antes de entregarlos[cite: 4].
- **Implementación de Contratos:** Aquí se implementan las `interfaces` declaradas en `Domain` (`RepoImp` implementa `RepoInterface`)[cite: 3, 4].

---

## 🗂️ 2. Menú de Navegación de Carpetas

Usa esta guía rápida para saber dónde encontrar o agregar tu código dentro de `data`[cite: 14]:

| Carpeta                        | ¿Qué hay aquí?                                                                        | ¿Para qué sirve? / Buscar aquí si necesitas...                                                                                               |
| :----------------------------- | :------------------------------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------------- |
| 🔌 **`dataSource/`**[cite: 14] | APIs (Retrofit/Ktor), Clientes de Supabase, DAOs locales y **DTOs**[cite: 4].         | Modificar llamadas HTTP, endpoints, queries SQL o ajustar clases de respuesta JSON con `@SerializedName`[cite: 4].                           |
| 🛠️ **`repoImp/`**[cite: 14]    | Implementaciones reales de los Repositorios y sus Mappers (`DTO -> Entity`)[cite: 4]. | Ajustar cómo se combinan las fuentes de datos (red vs. local), hacer llamadas a la API y mapear respuestas a `Entities` de Dominio[cite: 4]. |

---

> ⚠️ **Regla de Oro en Data:** Todo lo que entre o salga hacia `Domain` debe estar mapeado a una `Entity`[cite: 4]. Los DTOs jamás deben escaparse hacia las capas superiores[cite: 3, 4].
