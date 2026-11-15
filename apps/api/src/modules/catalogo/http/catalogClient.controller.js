import dogModel from "./modelosCatalogo/dog.model.js";
import behaviorModel from "./modelosCatalogo/behaviour.model.js";
import specialModel from "./modelosCatalogo/special.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";
import breedModel from "./modelosCatalogo/breed.model.js";
import sizeModel from "./modelosCatalogo/size.model.js";
import { leerFiltrosPerros } from "./dogFilters.js";

// HU24: opciones que el cliente puede elegir en los filtros del catálogo.
// Las columnas de texto de la BD pueden traer relleno (char(n)), por eso se recorta el nombre.
const aOpcion = (idColumna) => (fila) => ({
  id: fila[idColumna],
  nombre: String(fila.nombre ?? "").trim(),
});

export const getFiltros = async (req, res) => {
  try {
    const [razas, tamanos, caracteres] = await Promise.all([
      breedModel.getBreeds(),
      sizeModel.getSizes(),
      behaviorModel.getBehaviours(),
    ]);

    res.status(200).json({
      success: true,
      data: {
        razas: razas.map(aOpcion("id_raza")),
        tamanos: tamanos.map(aOpcion("id_tamano")),
        caracteres: caracteres.map(aOpcion("id_caracter")),
      },
    });
  } catch (error) {
    res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar las opciones de filtro.",
    });
  }
};

export const getDogById = async (req, res) => {
  console.log("ID recibido:", req.params.id); // Agrega este log para verificar el ID recibido
  try {
    const id = req.params.id;
    // Size & Breed are already included in the dogModel.getDogById function, so we don't need to call them separately.
    const [dogInfo, behaviorInfo, specialInfo, multimediaInfo] = await Promise.all([
      // Obtener la información del perro
      dogModel.getDogById(id),
      // Obtener la información del caracter
      behaviorModel.getDogBehaviourById(id),
      // Obtener la información de Características especiales
      specialModel.getDogSpecialConditionById(id),
      // Obtener la información de Multimedia
      multimediaModel.getDogMultimediaById(id),
    ]);

    console.log("Información del perro:", dogInfo);

    res.status(200).json({
      success: true,
      data: {
        dog: dogInfo,
        behavior: behaviorInfo,
        specialCondition: specialInfo,
        multimedia: multimediaInfo,
      },
    });
  } catch (error) {
    return res.status(500).json({
      sucess: false,
      message: error.message || "Error interno al consultar el perro.",
    });
  }
};

export const getDogs = async (req, res) => {
  // HU24: filtros opcionales (sexo, tamano, raza, edad, caracter). Sin filtros todo sigue igual.
  const { filtros, errores } = leerFiltrosPerros(req.query);
  const invalidos = Object.keys(errores);
  if (invalidos.length > 0) {
    return res.status(400).json({
      success: false,
      message: `Parámetro inválido: ${invalidos.join(", ")}`,
    });
  }

  try {
    const perros = await dogModel.getDogs(filtros);
    for (const perro of perros) {
      const fotos = await multimediaModel.getDogMultimediaById(perro.id_perro);
      console.log("Fotos encontradas:", fotos);
      if (fotos.length > 0) {
        perro.foto_url = fotos[0].url;
      } else {
        perro.foto_url = "";
      }
    }
    res.status(200).json({
      syuccess: true,
      data: perros,
    });
  } catch (error) {
    // 22P02: un id (tamano, raza o caracter) no tiene el formato de la columna, p. ej. un uuid mal escrito
    if (error.code === "22P02") {
      return res.status(400).json({
        success: false,
        message: "Parámetro inválido: tamano, raza o caracter",
      });
    }
    res.status(500).json({
      success: false,
      message: error.message,
    });
  }
};

//
