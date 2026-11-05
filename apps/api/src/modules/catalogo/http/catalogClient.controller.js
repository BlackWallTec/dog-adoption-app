import fs from "fs";
import path from "path";

import dogModel from "./modelosCatalogo/dog.model.js";
import breedModel from "./modelosCatalogo/breed.model.js";
import sizeModel from "./modelosCatalogo/size.model.js";
import behaviorModel from "./modelosCatalogo/behaviour.model.js";
import specialModel from "./modelosCatalogo/special.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";
import characterModel from "./modelosCatalogo/character.model.js";

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

const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const SEXOS = { macho: "Macho", hembra: "Hembra" };
const EDADES = ["cachorro", "adulto", "senior"];

// GET /api/catalogo/cliente/filtros -> opciones para la hoja de filtros
export const getFilterOptions = async (req, res) => {
  try {
    const [razas, tamanos, caracteres] = await Promise.all([
      breedModel.getBreeds(),
      sizeModel.getSizes(),
      characterModel.getCharacters(),
    ]);

    return res.status(200).json({
      success: true,
      data: {
        razas: razas.map((r) => ({ id: r.id_raza, nombre: r.nombre })),
        tamanos: tamanos.map((t) => ({ id: t.id_tamano, nombre: t.nombre })),
        caracteres: caracteres.map((c) => ({ id: c.id_caracter, nombre: c.nombre })),
      },
    });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar las opciones de filtro.",
    });
  }
};

// GET /api/catalogo/cliente/perros?sexo=&tamano=&raza=&edad=&caracter=
// Todos los parámetros son opcionales; sin parámetros devuelve la lista completa.
export const getDogsFiltered = async (req, res) => {
  const { sexo, tamano, raza, edad, caracter } = req.query;
  const filters = {};
  const invalido = (campo) =>
    res.status(400).json({ success: false, message: `Valor inválido para ${campo}.` });

  if (sexo !== undefined) {
    const valor = SEXOS[String(sexo).toLowerCase()];
    if (!valor) return invalido("sexo");
    filters.sexo = valor;
  }
  if (edad !== undefined) {
    const valor = String(edad).toLowerCase();
    if (!EDADES.includes(valor)) return invalido("edad");
    filters.edad = valor;
  }
  for (const [campo, valor] of [
    ["tamano", tamano],
    ["raza", raza],
  ]) {
    if (valor !== undefined) {
      if (!UUID_RE.test(String(valor))) return invalido(campo);
      filters[campo] = String(valor);
    }
  }
  if (caracter !== undefined) {
    const ids = String(caracter)
      .split(",")
      .map((id) => id.trim())
      .filter(Boolean);
    if (ids.length === 0 || ids.length > 10 || !ids.every((id) => UUID_RE.test(id))) {
      return invalido("caracter");
    }
    filters.caracteres = ids;
  }

  try {
    const perros = await dogModel.getDogsFiltered(filters);
    return res.status(200).json({ success: true, data: perros });
  } catch (error) {
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar los perros.",
    });
  }
};
