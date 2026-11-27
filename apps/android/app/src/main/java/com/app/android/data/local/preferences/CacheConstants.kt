package com.app.android.data.local.preferences

object CacheConstants {
    const val PREF_NAME = "dog_catalog_cache_prefs"
    const val KEY_DOGS_JSON = "cached_dogs_json"
    const val KEY_LAST_UPDATE = "cached_last_update"
    const val KEY_OFFSET = "cached_offset"
    const val KEY_TOTAL_COUNT = "cached_total_count"

    // Tiempo de validez del caché: 5 minutos en milisegundos
    const val CACHE_DURATION_MS = 5 * 60 * 1000L
}
