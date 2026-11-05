package com.app.android.domain.entities.entitiesSolicitudes

// ENTITY: el borrador completo de la solicitud, mientras el usuario recorre los 6 pasos.
// Vive solo en memoria (BorradorRepositoryImpl) hasta que se pica "Enviar solicitud".
// null en un paso = ese paso todavía no se ha llenado.
// Archivo compartido por todo el equipo: no cambiar sin avisar en el grupo. PORFAVOR
data class BorradorSolicitud(
    val idUsuario: String,
    val idPerro: String,
    val aceptoRequisitos: Boolean = false, // Paso 1 · Requisitos
    val datosGenerales: DatosGenerales? = null, // Paso 2 · Datos generales
    val tuHogar: TuHogar? = null, // Paso 3 · Tu hogar
    val compromiso: Compromiso? = null, // Paso 4 · Compromiso
    val documentos: DocumentosSolicitud? = null, // Paso 5 · Documentos
)

// Paso 3 · Tu hogar
data class TuHogar(
    val tuvoMascotas: Boolean? = null, // Sí / No ; null = sin contestar
    val otrosAnimales: Boolean? = null,
    val porQueAdoptar: String = "",
    val dondeVivira: String = "",
    val personas: Int = 1, // contador, mínimo 1
    val todosDeAcuerdo: Boolean? = null,
    val tipoVivienda: TipoVivienda? = null,
    val jardinPatio: String = "",
    val caseroAcepta: Boolean? = null, // null = no renta / no contesto
    val sobreMascotas: String = "",
)

enum class TipoVivienda { CASA, DEPARTAMENTO, OTRO }

// Paso 4 · Compromiso
data class Compromiso(
    val cuidadosBasicos: String = "",
    val opinionEsterilizacion: String = "",
    val aceptaSeguimiento: Boolean? = null,
)

// Paso 5 · Documentos: archivos elegidos en el telefono, todavaa NO subidos
data class DocumentosSolicitud(
    val identificacion: DocumentoLocal? = null,
    val comprobanteDomicilio: DocumentoLocal? = null,
)

data class DocumentoLocal(
    val uri: String, // "content://..." como texto (domain no conoce android.net.Uri)
    val nombre: String, // INE_Sofia_Garcia.pdf
    val tamanoBytes: Long,
    val mimeType: String, // application/pdf, image/jpeg, image/png
)