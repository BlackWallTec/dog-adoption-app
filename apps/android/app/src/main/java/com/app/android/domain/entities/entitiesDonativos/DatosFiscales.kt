package com.app.android.domain.entities.entitiesDonativos

// ENTITY: los datos fiscales del donante (HU45).
// Se capturan una vez y se reusan cada que pida un CFDI.
data class DatosFiscales(
    val rfc: String = "",
    val razonSocial: String = "",
    val regimenFiscal: String = "", // la clave del SAT, ej. "626"
    val codigoPostal: String = "",
    val correoFacturacion: String = "",
    val constanciaUrl: String? = null, // null = todavía no sube su constancia
) {
    // Si falta algo se le muestra el aviso de "aún no has guardado tus datos"
    val completos: Boolean
        get() =
            rfc.isNotBlank() &&
                razonSocial.isNotBlank() &&
                regimenFiscal.isNotBlank() &&
                codigoPostal.isNotBlank() &&
                constanciaUrl != null
}

// El archivo que escogió el usuario, ya leído (no sabe nada de Android)
class ConstanciaArchivo(
    val nombre: String,
    val tipo: String, // "application/pdf", "image/jpeg" o "image/png"
    val bytes: ByteArray,
)

// Catálogo de regímenes del SAT que más usan los donantes
// (clave, descripción). En la BD solo se guarda la clave.
val REGIMENES_FISCALES =
    listOf(
        "601" to "General de Ley Personas Morales",
        "603" to "Personas Morales con Fines no Lucrativos",
        "605" to "Sueldos y Salarios e Ingresos Asimilados a Salarios",
        "606" to "Arrendamiento",
        "612" to "Personas Físicas con Actividades Empresariales y Profesionales",
        "616" to "Sin obligaciones fiscales",
        "621" to "Incorporación Fiscal",
        "625" to "Actividades Empresariales con ingresos a través de Plataformas Tecnológicas",
        "626" to "Régimen Simplificado de Confianza",
    )
