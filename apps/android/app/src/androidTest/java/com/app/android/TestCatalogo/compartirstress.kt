package com.app.android.TestCatalogo

import kotlinx.coroutines.*
import org.junit.Test
import org.junit.Assert.assertTrue
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

class CompartirStressTest {

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

            println("URL: $enlace")
            println("Código HTTP: $codigo")

            assertTrue("Error HTTP: $codigo", codigo == 200)
        } finally {
            conexion.disconnect()
        }
    }

    @Test
    fun pruebaDeEstres() = runBlocking {

        probarConexion()

        val niveles = listOf(50, 100, 300, 500)

        val exitosas = AtomicInteger(0)
        val fallidas = AtomicInteger(0)
        val usuarios = mutableListOf<Job>()

        for (nivel in niveles) {

            val nuevosUsuarios = nivel - usuarios.size

            repeat(nuevosUsuarios) {
                usuarios.add(launch(Dispatchers.IO) {
                    while (isActive) {
                        realizarPeticion(exitosas, fallidas)
                        delay(1000)
                    }
                })
            }

            println("==============================")
            println("Usuarios virtuales: $nivel")

            delay(30000)

            println("Solicitudes exitosas: ${exitosas.get()}")
            println("Solicitudes fallidas: ${fallidas.get()}")
        }

        usuarios.forEach {
            it.cancel()
        }

        usuarios.joinAll()

        println("===== RESULTADOS DE ESTRÉS =====")
        println("Total exitosas: ${exitosas.get()}")
        println("Total fallidas: ${fallidas.get()}")
        println("Total: ${exitosas.get() + fallidas.get()}")

        assertTrue(
            "Ninguna solicitud fue exitosa",
            exitosas.get() > 0
        )
    }

    private fun realizarPeticion(
        exitosas: AtomicInteger,
        fallidas: AtomicInteger
    ) {
        var conexion: HttpURLConnection? = null

        try {
            conexion = URL(enlace)
                .openConnection() as HttpURLConnection

            conexion.requestMethod = "GET"
            conexion.connectTimeout = 5000
            conexion.readTimeout = 5000

            val codigo = conexion.responseCode

            if (codigo == 200) {
                exitosas.incrementAndGet()
            } else {
                fallidas.incrementAndGet()
                println("Error HTTP: $codigo")
            }

        } catch (e: Exception) {
            fallidas.incrementAndGet()
            println("Error de conexión: ${e.message}")
        } finally {
            conexion?.disconnect()
        }
    }
}