package com.app.android.data.local.preferences

import android.content.Context
import androidx.core.content.edit
import com.app.android.data.local.models.DogCacheEntity
import com.app.android.data.local.models.toCacheEntity
import com.app.android.domain.entities.entitiesCatalogo.DogSummary
import com.app.android.domain.entities.entitiesCatalogo.PaginatedDogs
import com.app.android.domain.entities.entitiesCatalogo.PaginationMeta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DogPreferences
    @Inject
    constructor(
        @ApplicationContext context: Context,
        private val gson: Gson,
    ) {
        private val prefs = context.getSharedPreferences(CacheConstants.PREF_NAME, Context.MODE_PRIVATE)

        fun savePaginatedDogs(
            newDogs: List<DogSummary>,
            newOffset: Int,
            totalCount: Int,
            isFirstPage: Boolean,
        ) {
            // Si es la primera página (refresh o busqueda nueva), reemplazamos.
            // Si es página siguiente (scroll infinito), concatenamos con lo que ya existía.
            val currentList =
                if (isFirstPage) {
                    emptyList()
                } else {
                    getCachedDogsEntityList()
                }

            val updatedList =
                (currentList + newDogs.map { it.toCacheEntity() })
                    .distinctBy { it.id } // Evita perros duplicados por seguridad

            prefs.edit {
                putString(CacheConstants.KEY_DOGS_JSON, gson.toJson(updatedList))
                putInt(CacheConstants.KEY_OFFSET, newOffset)
                putInt(CacheConstants.KEY_TOTAL_COUNT, totalCount)

                // Reiniciamos el reloj de expiración (TTL) solo en recargas frescas de página 1
                if (isFirstPage) {
                    putLong(CacheConstants.KEY_LAST_UPDATE, System.currentTimeMillis())
                }
            }
        }

        fun getCachedPaginatedDogs(): PaginatedDogs? {
            val entities = getCachedDogsEntityList()
            if (entities.isEmpty()) return null

            val savedOffset = prefs.getInt(CacheConstants.KEY_OFFSET, entities.size)
            val totalCount = prefs.getInt(CacheConstants.KEY_TOTAL_COUNT, entities.size)
            val hasMore = savedOffset < totalCount

            return PaginatedDogs(
                dogs = entities.map { it.toDomain() },
                meta =
                    PaginationMeta(
                        total = totalCount,
                        hasMore = hasMore,
                        perPage = entities.size,
                    ),
            )
        }

        fun isCacheValid(): Boolean {
            val lastUpdate = prefs.getLong(CacheConstants.KEY_LAST_UPDATE, 0L)
            if (lastUpdate == 0L) return false
            // Válido por 5 minutos (configurado en CacheConstants.CACHE_DURATION_MS)
            return (System.currentTimeMillis() - lastUpdate) < CacheConstants.CACHE_DURATION_MS
        }

        fun clearCache() {
            prefs.edit { clear() }
        }

        private fun getCachedDogsEntityList(): List<DogCacheEntity> {
            val json = prefs.getString(CacheConstants.KEY_DOGS_JSON, null) ?: return emptyList()
            val type = object : TypeToken<List<DogCacheEntity>>() {}.type
            return try {
                gson.fromJson(json, type) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
