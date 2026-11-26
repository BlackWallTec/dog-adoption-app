package com.app.android.TestCatalogo

import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.assertTrue
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

class CompartirLoadTest {

    private val enlace =
        "http://192.168.1.66:3000/api/catalogo/cliente/perros"

    @Test
    fun probarConexion() {
        val conexion = URL(enlace)
            .openConnection() as HttpURLConnection

        try {
            conexion.requestMethod = "GET"
            conexion.connectTimeout = 5000
            conexion.readTimeout = 5000

            val codigo = conexion.responseCode

            println("Código HTTP: $codigo")

            assertTrue(
                "Error HTTP: $codigo",
                codigo == 200
            )
        } finally {
            conexion.disconnect()
        }
    }

    @Test
    fun pruebaDeCarga() = runBlocking {

        probarConexion()

        val exitosas = AtomicInteger(0)
        val fallidas = AtomicInteger(0)
        val tiempoTotal = AtomicLong(0)
        val usuarios = mutableListOf<Job>()

        val codigosHTTP =
            ConcurrentHashMap<Int, AtomicInteger>()

        // Subida: 15 segundos
        println("Iniciando subida a 50 usuarios")

        repeat(50) {
            usuarios.add(launch(Dispatchers.IO) {
                while (isActive) {
                    realizarPeticion(
                        exitosas,
                        fallidas,
                        tiempoTotal,
                        codigosHTTP
                    )
                    delay(1000)
                }
            })

            delay(300)
        }

        // Meseta: 30 segundos
        println("Manteniendo 50 usuarios")
        delay(30000)

        // Bajada: 10 segundos
        println("Disminuyendo usuarios")

        usuarios.forEach {
            it.cancel()
            delay(200)
        }

        usuarios.joinAll()

        val total = exitosas.get() + fallidas.get()

        val porcentajeExito = if (total > 0) {
            exitosas.get() * 100.0 / total
        } else {
            0.0
        }

        val porcentajeError = if (total > 0) {
            fallidas.get() * 100.0 / total
        } else {
            0.0
        }

        val tiempoPromedio = if (total > 0) {
            tiempoTotal.get().toDouble() / total
        } else {
            0.0
        }

        println("===== RESULTADOS DE CARGA =====")
        println("Usuarios virtuales: 50")
        println("Solicitudes totales: $total")
        println("Solicitudes exitosas: ${exitosas.get()}")
        println("Solicitudes fallidas: ${fallidas.get()}")
        println("Porcentaje de éxito: $porcentajeExito%")
        println("Porcentaje de error: $porcentajeError%")
        println("Tiempo promedio: $tiempoPromedio ms")

        println("===== CÓDIGOS HTTP =====")

        codigosHTTP.forEach { (codigo, cantidad) ->
            println("HTTP $codigo: ${cantidad.get()}")
        }

        assertTrue(
            "Porcentaje de éxito: $porcentajeExito%. " +
                    "Exitosas: ${exitosas.get()}. " +
                    "Fallidas: ${fallidas.get()}. " +
                    "Códigos HTTP: $codigosHTTP",
            total > 0 && fallidas.get() == 0
        )
    }

    private fun realizarPeticion(
        exitosas: AtomicInteger,
        fallidas: AtomicInteger,
        tiempoTotal: AtomicLong,
        codigosHTTP: ConcurrentHashMap<Int, AtomicInteger>
    ) {
        var conexion: HttpURLConnection? = null

        val inicio = System.nanoTime()

        try {
            conexion = URL(enlace)
                .openConnection() as HttpURLConnection

            conexion.requestMethod = "GET"
            conexion.connectTimeout = 5000
            conexion.readTimeout = 5000

            val codigo = conexion.responseCode

            codigosHTTP
                .computeIfAbsent(codigo) {
                    AtomicInteger(0)
                }
                .incrementAndGet()

            if (codigo == 200) {
                conexion.inputStream.use {
                    it.readBytes()
                }

                exitosas.incrementAndGet()
            } else {
                fallidas.incrementAndGet()
                println("Error HTTP: $codigo")
            }

        } catch (e: Exception) {
            fallidas.incrementAndGet()
            println("Error de conexión: ${e.message}")

        } finally {
            val duracion =
                (System.nanoTime() - inicio) / 1_000_000

            tiempoTotal.addAndGet(duracion)

            conexion?.disconnect()
        }
    }
}