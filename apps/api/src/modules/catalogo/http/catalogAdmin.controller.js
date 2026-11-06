import dogModel from "./modelosCatalogo/dog.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";
import behaviourModel from "./modelosCatalogo/behaviour.model.js";
import cuidadoEspecialModel from "./modelosCatalogo/special.model.js";
import sizeModel from "./modelosCatalogo/size.model.js";
import breedModel from "./modelosCatalogo/breed.model.js";

import { supabase } from "../../../config/supabaseClient.js"; //Client de Supabase para operaciones directas si es necesario

//===== Funciones de Subida de Documentos SUPEROPTIMIZADOS ====
// Nosotros en Catalogo neceistamos que la subida de documentos sea lo más rápida posible, por eso no hacemos validaciones de tipo ni de tamaño ni de extensión.
// Ruta de llamada
export const obtenerUrlSubidaMultimedia = async (req, res) => {
  try {
    //1. Obtener el ID del perro y los datos del archivo desde la solicitud
    const { id_perro_temporal, nombreArchivo, tipoMime } = req.body; // Ej: "foto_perfil.jpg", "image/jpeg"

    //2. Validar que se recibieron los datos necesarios
    if (!id_perro_temporal || !nombreArchivo || !tipoMime) {
      return res
        .status(400)
        .json({ error: "Faltan datos del archivo (id_perro_temporal, nombreArchivo o tipoMime)" });
    }

    // 3. Definimos una ruta única dentro del bucket de Supabase
    const rutaArchivo = `perros/${id_perro_temporal}/${Date.now()}_${nombreArchivo}`;

    //4. Solcitiamos a Supabase la URL de subida (Expira en 5 minutos / 300 segundos)
    const { data, error } = await supabase.storage
      .from("multimedia_perros") //Nombre del bucket en Supabase
      .createSignedUploadUrl(rutaArchivo, 300); // URL válida por 300 segundos

    if (error) throw error;
    const { data: publicUrlData } = supabase.storage
      .from("multimedia_perros")
      .getPublicUrl(rutaArchivo);

    //5. Retornamos la URL especial de subida y la URL final donde quedará el archivo
    return res.status(200).json({
      message: "Pase de subida generado exitosamente",
      uploadUrl: data.signedUrl, // URL para subir el archivo
      rutaFinal: rutaArchivo, // Ruta final donde quedará el archivo en el bucket
      publicUrl: publicUrlData.publicUrl,
    });
  } catch (error) {
    console.error("Error al generar URL firmada:", error.message);
    return res.status(500).json({ error: "No se pudo generar la URL de subida" });
  }
};

// ===== FIN Funciones de Subida de Documentos SUPEROPTIMIZADOS ====

// ===== Funciones GET Admin =====

export const getPerrosAdmin = async (req, res) => {
  try {
    const perros = await dogModel.getDogs();
    return res.status(200).json({
      success: true,
      data: perros,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los perros.",
    });
  }
};

// Get Tamaños
export const getTamanos = async (req, res) => {
  try {
    const tamanos = await sizeModel.getSizes();
    return res.status(200).json({
      success: true,
      data: tamanos,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los tamaños.",
    });
  }
};

// Get Características
export const getCaracteristicas = async (req, res) => {
  try {
    const caracteristicas = await behaviourModel.getBehaviours();
    return res.status(200).json({
      success: true,
      data: caracteristicas,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar las características.",
    });
  }
};

// Get Cuidado Especial
export const getCuidadoEspecial = async (req, res) => {
  try {
    const cuidados = await cuidadoEspecialModel.getSpecialConditions();
    return res.status(200).json({
      success: true,
      data: cuidados,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los cuidados especiales.",
    });
  }
};

//Get getRazas
export const getRazas = async (req, res) => {
  try {
    const razas = await breedModel.getBreeds();
    return res.status(200).json({
      success: true,
      data: razas,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar las razas.",
    });
  }
};

// ===== Fin Funciones GET Admin =====

// ===== Funciónes POST ====

// ===== Función POST Nuevo Perro (HU19)====

//funcion auxiliar de coercion de tipos
function mapToTypedValue(value, targetType) {
  // Forzar el tipo de dato según targetType, devolviendo un array con [isValid, cleanedValue]
  //En caso de que el valor no pueda ser convertido al tipo deseado, isValid será false y cleanedValue será null.
  switch (targetType) {
    case "string":
      return [true, String(value)];
    case "number":
      const num = Number(value);
      return [Number.isFinite(num), num];
    case "boolean":
      if (value === true || value === "true") return [true, true];
      if (value === false || value === "false") return [true, false];
      return [false, null];
    default:
      return [false, null];
  }
}

// funcion auxiliar para mapear el req entrante a un objeto que pueda ser insertado en la base de datos.
// Regresa 2 datos (bool & objeto) para saber si el mapeo fue exitoso o no.
function mapRequestToDogData(reqBody) {
  // CORREGIDO: id_raza e id_tamano ahora inician como string "" para coincidir con los UUIDs
  var dogData = {
    nombre: "",
    sexo: "",
    edad_aprox: 0,
    tamano_medida: 0.0,
    descripcion: "",
    esterilizado: false,
    vacunado: false,
    estado: "",
    id_raza: "", // <-- Cambiado de 0 a ""
    id_tamano: "", // <-- Cambiado de 0 a ""
    multimedia: [],
    caracteres: [],
    cuidados: [],
  };

  var isValid = true;
  var errorMessages = [];
  const optionalFields = new Set(["edad_aprox", "tamano_medida", "descripcion"]);

  // Expresión regular para validar estructuras UUID v4 estándar
  const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[4][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

  for (const key in dogData) {
    if (Object.prototype.hasOwnProperty.call(reqBody, key)) {
      const value = reqBody[key];

      if (value === null || value === undefined || value === "") {
        if (optionalFields.has(key)) continue;
        isValid = false;
        errorMessages.push(`El campo obligatorio '${key}' está vacío.`);
        break;
      }

      let cleanedValue = value;

      if (Array.isArray(dogData[key])) {
        if (!Array.isArray(value)) {
          isValid = false;
          errorMessages.push(`El campo '${key}' debe ser un array.`);
          break;
        }

        // VALIDACIÓN: Asegurar que los elementos dentro de caracteres y cuidados sean strings UUID válidos
        if (key === "caracteres" || key === "cuidados") {
          const allElementsValid = value.every(
            (id) => typeof id === "string" && uuidRegex.test(id),
          );
          if (!allElementsValid) {
            isValid = false;
            errorMessages.push(`El array '${key}' contiene uno o más UUIDs inválidos.`);
            break;
          }
        }
      } else if (typeof dogData[key] === "object" && dogData[key] !== null) {
        if (typeof value !== "object" || value === null) {
          isValid = false;
          errorMessages.push(`El campo '${key}' debe ser un objeto.`);
          break;
        }
      } else {
        // VALIDACIÓN: Verificar si los campos id_raza o id_tamano son UUID válidos antes de parsear
        if (key === "id_raza" || key === "id_tamano") {
          if (typeof value !== "string" || !uuidRegex.test(value)) {
            isValid = false;
            errorMessages.push(`El campo '${key}' no tiene un formato UUID válido.`);
            break;
          }
        }

        const [valid, parsedValue] = mapToTypedValue(value, typeof dogData[key]);
        if (!valid) {
          isValid = false;
          errorMessages.push(`El campo '${key}' tiene un tipo de valor inválido.`);
          break;
        }
        cleanedValue = parsedValue;
      }
      dogData[key] = cleanedValue;
    } else {
      isValid = false;
      errorMessages.push(`Falta el campo requerido '${key}' en la solicitud.`);
      break;
    }
  }

  return { isValid, dogData, errorMessages };
  /* Formato de respues del POST 
{
  "nombre": "Firulais",
  "sexo": "Macho",
  "edad_aprox": 3,
  "tamano_medida": 45.5,
  "descripcion": "Un perro muy amigable y juguetón.",
  "esterilizado": true,
  "vacunado": true,
  "estado": "Disponible",
  "id_raza": "UUID",
  "id_tamano": "UUID",
  "multimedia": [
    { "url": "https://storage.com", "tipo": "imagen" }
  ],
  "caracteres": [{ "id_caracter": "UUID" }, { "id_caracter": UUID }],
  "cuidados": [{ "id_cuidado": "UUID" }, { "id_cuidado": UUID }]
}
  */
}

export const postNuevoPerro = async (req, res) => {
  console.log("POST receiving call: ", req.body);
  try {
    const dogData = req.body;
    console.log("Datos recibidos para nuevo perro:", dogData);

    // Validar y mapear el reqBody a un objeto dogData
    const { isValid, dogData: mappedDogData, errorMessages } = mapRequestToDogData(dogData);

    if (!isValid) {
      //Imprimir los mensajes de error en la consola para debug
      logErrorMessages(errorMessages);
      //Responder con un error 400 al cliente
      return res.status(400).json({
        success: false,
        message: "Datos inválidos en la solicitud.",
        errors: errorMessages,
      });
    }

    // Crear el nuevo perro en la base de datos mediante Stored Procedure (RPC) que maneja la inserción en todas las tablas relacionadas
    // 1. Inserción en tabla Perro: (Incluye Raza & Tamaño)
    const perroCreado = await dogModel.createFullDog(mappedDogData);

    console.log("🎉 OPERACIÓN EXITOSA EN SUPABASE:");

    return res.status(200).json({
      success: true,
      data: perroCreado,
    });
  } catch (error) {
    console.error("🚨 DETALLE COMPLETO DEL ERROR DESDE SUPABASE:", error);
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al crear el perro.",
    });
  }
};

// ===== Fin Función POST Nuevo Perro ====

// ===== Función PATCH Desactivar Perro ====

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-8][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;

export const desactivarPerro = async (req, res) => {
  try {
    const { id } = req.params;

    if (!UUID_REGEX.test(id || "")) {
      return res.status(400).json({
        success: false,
        message: "El id del perro no tiene un formato UUID válido.",
      });
    }

    const perro = await dogModel.desactivarPerro(id);

    if (!perro) {
      return res.status(404).json({
        success: false,
        message: "No se encontró el perro.",
      });
    }

    return res.status(200).json({
      success: true,
      data: perro,
    });
  } catch (error) {
    console.error("Error al desactivar el perro:", error);
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al desactivar el perro.",
    });
  }
};

// ===== Fin Función PATCH Desactivar Perro ====

// funciones de debug general
function logErrorMessages(messages) {
  //1. Si messages es un array, iterar y loguear cada mensaje
  if (Array.isArray(messages)) {
    messages.forEach((msg) => console.error(msg));
  } else {
    //2. Si messages es un string, loguear directamente
    console.error(messages);
  }
}
