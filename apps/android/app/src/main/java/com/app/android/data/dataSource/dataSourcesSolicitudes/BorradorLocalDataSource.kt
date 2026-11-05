package com.app.android.data.dataSource.dataSourcesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.Compromiso
import com.app.android.domain.entities.entitiesSolicitudes.TuHogar
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

// Data source local: guarda los datos que el usuario escribe en los pasos del formulario
// antes de confirmar su solicitud (enviar a la BD)
// No habla con el backend: la view Confirmar lo hara
// Singleton para que se use la copia en toda la app
@Singleton
class BorradorLocalDataSource
    @Inject
    constructor() {
        // con IdSoilicitud se confirma que solo un usuario tenga su versión del borrador

        // Aqui añadan las variables de las views a cambiar
        private val tuHogar = ConcurrentHashMap<String, TuHogar>()
        private val compromiso = ConcurrentHashMap<String, Compromiso>()

        // ------- SOLICITUDES ---------
        // Paso 3
        fun getTuHogar(idSolicitud: String): TuHogar? = tuHogar[idSolicitud]

        fun guardarTuHogar(
            idSolicitud: String,
            datos: TuHogar,
        ) {
            tuHogar[idSolicitud] = datos
        }

        // Paso 4
        fun getCompromiso(idSolicitud: String): Compromiso? = compromiso[idSolicitud]

        fun guardarCompromiso(
            idSolicitud: String,
            datos: Compromiso,
        ) {
            compromiso[idSolicitud] = datos
        }

        // Para cuando confirmar mande la solicitud (o el usuario lo cancele)
        fun limpiar(idSolicitud: String) {
            // Añadan el resto
            tuHogar.remove(idSolicitud)
            compromiso.remove(idSolicitud)
        }
    }

// Para cambiar tu view de enviar a backend a enviar al borrador local sigue estos pasos

// 1. Añade los parametros a modificar aqui: tuhogar, compromiso, etc
// 2. En networkModule añade los parametros del borrador tipo.
// fun ejemplo {api: api, borrador: borrador(este}: ejemplo = repositoryimpl(api, borrador)
// Remplaza las llamadas del api en solicitudRepositoryImpl a las del borrador
