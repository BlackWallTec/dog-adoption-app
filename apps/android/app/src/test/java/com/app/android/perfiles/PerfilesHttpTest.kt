package com.app.android.perfiles

import com.app.android.data.dataSource.dataSourcesPerfiles.UsuarioAPIservice
import com.app.android.data.repoImp.repoImpPerfiles.UsuarioRepositoryImpl
import com.app.android.data.session.PerfilesAuthInterceptor
import com.app.android.domain.entities.entitiesPerfiles.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress
import java.util.Collections
import java.util.concurrent.Executors
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class PerfilesHttpTest {
    private data class Peticion(val path: String, val method: String, val body: String, val client: String?, val authorization: String?)
    private val requests = Collections.synchronizedList(mutableListOf<Peticion>())
    private val almacen = AlmacenSesionMemoria()
    private val executor = Executors.newSingleThreadExecutor()
    private lateinit var server: HttpServer
    private lateinit var client: OkHttpClient
    private lateinit var repository: UsuarioRepositoryImpl
    private val user = """{"id":"usuario-1","email":"perfiles.prueba@gmail.com","rol":"Cliente"}"""
    private val login = """{"user":$user,"token":"token-android-prueba"}"""
    private var reply: (Peticion) -> Pair<Int, String> = { 200 to login }

    @Before fun setup() {
        server = HttpServer.create(InetSocketAddress("127.0.0.1", 0), 0)
        server.executor = executor
        server.createContext("/") { exchange ->
            val request = Peticion(exchange.requestURI.path, exchange.requestMethod,
                exchange.requestBody.bufferedReader().readText(), exchange.requestHeaders.getFirst("x-client"), exchange.requestHeaders.getFirst("Authorization"))
            requests.add(request)
            val (status, body) = reply(request)
            val bytes = body.toByteArray(Charsets.UTF_8)
            exchange.responseHeaders.add("Content-Type", "application/json")
            exchange.sendResponseHeaders(status, bytes.size.toLong())
            exchange.responseBody.use { it.write(bytes) }
        }
        server.start()
        client = OkHttpClient.Builder().addInterceptor(PerfilesAuthInterceptor(almacen)).build()
        val api = Retrofit.Builder().baseUrl("http://127.0.0.1:${server.address.port}/").client(client).addConverterFactory(GsonConverterFactory.create()).build().create(UsuarioAPIservice::class.java)
        repository = UsuarioRepositoryImpl(api, almacen)
    }
    @After fun teardown() { server.stop(0); executor.shutdownNow(); client.dispatcher.executorService.shutdownNow(); client.connectionPool.evictAll() }

    @Test fun `registro envia el contrato mobile y guarda una sesion sin cookies`() = runBlocking {
        val result = repository.registrar(registroValido.copy(email = " PERFILES.PRUEBA@GMAIL.COM "))
        val sent = requests.single()
        assertEquals("/api/perfiles/register", sent.path)
        assertEquals("POST", sent.method)
        assertEquals("mobile", sent.client)
        assertNull(sent.authorization)
        val json = Gson().fromJson(sent.body, JsonObject::class.java)
        assertEquals("perfiles.prueba@gmail.com", json["email"].asString)
        assertEquals("0", json["numeroExterior"].asString)
        assertTrue(json["privacidad"].asBoolean)
        assertFalse(json.has("rol"))
        assertEquals(result, almacen.actual)
    }
    @Test fun `el perfil utiliza Bearer y convierte numero exterior cero correctamente`() = runBlocking {
        repository.login(registroValido.email, registroValido.passwd)
        reply = { 200 to """{"profile":{"nombre":"Ana","apellidos":"Prueba","telefono":"5512345678","fechaNacimiento":"2000-06-15","calle":"Calle de prueba","numeroExterior":0,"codigoPostal":"00000"}}""" }
        val profile = repository.consultarPerfil()
        assertEquals("Bearer token-android-prueba", requests.last().authorization)
        assertEquals("/api/perfiles/profile", requests.last().path)
        assertEquals("GET", requests.last().method)
        assertEquals("0", profile.numeroExterior)
        assertEquals("Ana", profile.nombre)
    }
    @Test fun `restaura la sesion con me y conserva el token del dispositivo`() = runBlocking {
        val saved = Sesion(Usuario("usuario-1", registroValido.email, "Cliente"), "token-guardado")
        almacen.guardar(saved)
        reply = { 200 to """{"user":$user}""" }
        assertEquals(saved, repository.restaurarSesion())
        assertEquals("/api/perfiles/me", requests.last().path)
        assertEquals("Bearer token-guardado", requests.last().authorization)
    }
    @Test fun `una sesion vencida se elimina y no se restaura como valida`() = runBlocking {
        almacen.guardar(Sesion(Usuario("usuario-1", registroValido.email, "Cliente"), "vencido"))
        reply = { 401 to """{"error":"Token expirado"}""" }
        assertNull(repository.restaurarSesion())
        assertNull(almacen.actual)
    }
    @Test fun `un error temporal no borra la sesion guardada`() {
        almacen.guardar(Sesion(Usuario("usuario-1", registroValido.email, "Cliente"), "guardado"))
        reply = { 500 to "{}" }
        assertThrows(FalloAutenticacion::class.java) { runBlocking { repository.restaurarSesion() } }
        assertNotNull(almacen.actual)
    }
    @Test fun `el correo duplicado se muestra como error de campo sin crear sesion`() {
        reply = { 409 to """{"fields":{"email":"Este correo ya está registrado."}}""" }
        val error = assertThrows(RegistroInvalidoException::class.java) { runBlocking { repository.registrar(registroValido) } }
        assertNotNull(error.campos["email"])
        assertNull(almacen.actual)
    }
    @Test fun `un menor no genera ninguna peticion HTTP`() {
        assertThrows(RegistroInvalidoException::class.java) { runBlocking { repository.registrar(registroValido.copy(fechaNacimiento = "2999-01-01")) } }
        assertTrue(requests.isEmpty())
    }
    @Test fun `cerrar sesion elimina el token aunque el servidor falle`() = runBlocking {
        repository.login(registroValido.email, registroValido.passwd)
        reply = { 500 to "{}" }
        repository.cerrarSesion()
        assertNull(almacen.actual)
        assertEquals("/api/perfiles/logout", requests.last().path)
        assertNull(requests.last().authorization)
    }
}
