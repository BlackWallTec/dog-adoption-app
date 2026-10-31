import fs from "fs";
import path from "path";

import dogModel from "./modelosCatalogo/dog.model.js";
import breedModel from "./modelosCatalogo/breed.model.js";
import sizeModel from "./modelosCatalogo/size.model.js";
import behaviorModel from "./modelosCatalogo/behaviour.model.js";
import specialModel from "./modelosCatalogo/special.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";

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
  try {
    const perros = await dogModel.getDogs();
    for (const perro of perros) {
      const fotos = await multimediaModel.getDogMultimediaById(perro.id_perro);
      if (fotos.lenght > 0) {
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
    res.status(500).json({
      success: false,
      message: error.message,
    });
  }
};
