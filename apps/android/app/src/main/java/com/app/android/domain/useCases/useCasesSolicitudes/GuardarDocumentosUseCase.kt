package com.app.android.domain.useCases.useCasesSolicitudes

import com.app.android.domain.entities.entitiesSolicitudes.DocumentoLocal
import com.app.android.domain.entities.entitiesSolicitudes.DocumentosSolicitud
import com.app.android.domain.repoInterface.repoIntSolicitudes.BorradorRepositoryInt
import javax.inject.Inject

data class ErroresDocumentos(
    val identificacion: String? = null,
    val comprobanteDomicilio: String? = null,
) {
    val hayErrores: Boolean
        get() = identificacion != null || comprobanteDomicilio != null
}

class GuardarDocumentosUseCase
    @Inject
    constructor(
        private val borrador: BorradorRepositoryInt,
    ) {
        operator fun invoke(documentos: DocumentosSolicitud): ErroresDocumentos {
            val errores =
                ErroresDocumentos(
                    identificacion = validarDocumento(documentos.identificacion, "identificación"),
                    comprobanteDomicilio =
                        validarDocumento(
                            documentos.comprobanteDomicilio,
                            "comprobante de domicilio",
                        ),
                )
            if (!errores.hayErrores) {
                borrador.actualizar { it.copy(documentos = documentos) }
            }
            return errores
        }

        private fun validarDocumento(
            doc: DocumentoLocal?,
            nombreCampo: String,
        ): String? {
            if (doc == null) return "Adjunta tu $nombreCampo"
            if (doc.mimeType !in FORMATOS_PERMITIDOS) return "Formato no permitido (usa PDF, JPG o PNG)"
            if (doc.tamanoBytes > TAMANO_MAX_BYTES) return "El archivo $nombreCampo supera los 5MB"
            if (doc.tamanoBytes <= 0L) return "El archivo $nombreCampo no puede estar vacio"
            return null
        }

        private companion object {
            val FORMATOS_PERMITIDOS = setOf("application/pdf", "image/jpeg", "image/png")
            const val TAMANO_MAX_BYTES = 5L * 1024 * 1024
        }
    }
