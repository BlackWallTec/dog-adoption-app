import dogModel from "./modelosCatalogo/dog.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";
import behaviourrModel from "./modelosCatalogo/behaviour.model.js";
import cuidadoEspecialModel from "./modelosCatalogo/special.model.js";

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

    //5. Retornamos la URL especial de subida y la URL final donde quedará el archivo
    return res.status(200).json({
      message: "Pase de subida generado exitosamente",
      uploadUrl: data.signedUrl, // URL para subir el archivo
      rutaFinal: rutaArchivo, // Ruta final donde quedará el archivo en el bucket
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
    const tamanos = await dogModel.getSizes();
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
    const caracteristicas = await behaviourrModel.getBehaviours();
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
    const cuidados = await cuidadoEspecialModel.getSpecialCare();
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
    const razas = await dogModel.getBreeds();
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
      return [!isNaN(num), num];
    case "boolean":
      if (value === "true") return [true, true];
      if (value === "false") return [true, false];
      return [false, null];
    default:
      return [false, null];
  }
}

// funcion auxiliar para mapear el req entrante a un objeto que pueda ser insertado en la base de datos.
// Regresa 2 datos (bool & objeto) para saber si el mapeo fue exitoso o no.
function mapRequestToDogData(reqBody) {
  var dogData = {
    nombre: "",
    sexo: "",
    edad_aprox: 0,
    tamano_medida: 0.0,
    descripcion: "",
    esterilizado: false,
    vacunado: false,
    estado: "",
    id_raza: 0,
    id_tamano: 0,
    multimedia: [],
    caracteres: [],
    cuidados: [],
  };
  var isValid = true;
  var errorMessages = []; //Array de errores para debug, no se envía al cliente por seguridad.
  // Validar y mapear los campos del reqBody al objeto dogData
  for (const key in dogData) {
    //1. Validamos que el reqBody tenga siquiera la propiedad
    if (reqBody.hasOwnProperty(key)) {
      //2. Validamos que el reqBody tenga un valor válido para la propiedad (mayor seguridad)
      const value = reqBody[key];
      //3. Validar estrictamente contra null y undefined para permitir 0 y false
      if (value !== null && value !== undefined && value !== "") {
        //3.1: Arrays & Objetos: Validar que sean del tipo correcto
        if (Array.isArray(dogData[key])) {
          if (!Array.isArray(value)) {
            isValid = false;
            errorMessages.push(`El campo '${key}' debe ser un array.`);
            break;
          }
          // 3.2: Objetos: Validar que sean del tipo correcto
        } else if (typeof dogData[key] === "object" && dogData[key] !== null) {
          if (typeof value !== "object" || value === null) {
            isValid = false;
            errorMessages.push(`El campo '${key}' debe ser un objeto.`);
            break;
          }
        } else {
          //3.3: Forzar tipo de dato
          var [valid, cleanedValue] = mapToTypedValue(value, typeof dogData[key]);
          if (!valid) {
            isValid = false;
            errorMessages.push(`El campo '${key}' tiene un valor inválido.`);
            break;
          }
        }
        dogData[key] = cleanedValue;
      } else {
        isValid = false;
        break;
      }
    } else {
      isValid = false;
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
  "id_raza": 1,
  "id_tamano": 2,
  "multimedia": [
    { "url": "https://storage.com", "tipo": "imagen" }
  ],
  "caracteres": [1, 3], 
  "cuidados": [2, 4]      
}
  */
}

export const postNuevoPerro = async (req, res) => {
  console.log("POST /api/catalogo/admin/perros");
  try {
    const dogData = req.body;
    log("Datos recibidos para nuevo perro:", dogData);

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

    return res.status(200).json({
      success: true,
      data: perroCreado,
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al crear el perro.",
    });
  }
};

// ===== Fin Función POST Nuevo Perro ====

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
