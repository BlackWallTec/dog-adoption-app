package com.app.android.data.dataSource.dataSourcesVoluntariado

import android.util.Log
import com.app.android.domain.entities.entitiesVoluntariado.Resultado
import com.app.android.domain.entities.entitiesVoluntariado.VoluntariadoError
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

private const val TAG = "VoluntariadoApi"

suspend fun <T> safeApiCall(
    gson: Gson,
    llamada: suspend () -> T,
): Resultado<T> =
    try {
        Resultado.Exito(llamada())
    } catch (e: CancellationException) {
        throw e
    } catch (e: HttpException) {
        Log.e(TAG, "HTTP ${e.code()}", e)
        Resultado.Error(e.toVoluntariadoError(gson))
    } catch (e: IOException) {
        Log.e(TAG, "Error de red", e)
        Resultado.Error(VoluntariadoError.SinConexion)
    } catch (e: Exception) {
        Log.e(TAG, "Error inesperado", e)
        Resultado.Error(VoluntariadoError.Desconocido)
    }

private fun HttpException.toVoluntariadoError(gson: Gson): VoluntariadoError {
    val codigo =
        runCatching {
            gson.fromJson(response()?.errorBody()?.string(), ErrorApiDto::class.java)?.code
        }.getOrNull()

    return when (codigo) {
        "SIN_CUPO" -> VoluntariadoError.SinCupo
        "YA_INSCRITO" -> VoluntariadoError.YaInscrito
        "PROGRAMA_INICIADO" -> VoluntariadoError.ProgramaIniciado
        "PROGRAMA_NO_DISPONIBLE" -> VoluntariadoError.ProgramaNoDisponible
        "PROGRAMA_NO_ENCONTRADO" -> VoluntariadoError.ProgramaNoEncontrado
        "NO_AUTENTICADO" -> VoluntariadoError.NoAutenticado
        else -> if (code() == 401) VoluntariadoError.NoAutenticado else VoluntariadoError.Desconocido
    }
}