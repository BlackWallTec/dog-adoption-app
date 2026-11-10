package android.util

/*
 * ¿POR QUÉ EXISTE ESTE ARCHIVO?
 *
 * Las pruebas unitarias corren en la JVM de tu computadora, NO en un teléfono.
 * Ahí, las clases de Android son "cascarones vacíos": si el código llama a
 * `Log.e(...)`, la prueba truena con "Method e in android.util.Log not mocked".
 *
 * `safeApiCall` usa `Log.e`, así que aquí creamos un `Log` de mentira con el mismo
 * nombre y paquete. Al correr pruebas, Java encuentra primero esta versión.
 *
 * Solo existe en `src/test`: nunca llega a la app real.
 */
object Log {
    @JvmStatic
    fun e(tag: String?, msg: String?, tr: Throwable?): Int = 0
}
