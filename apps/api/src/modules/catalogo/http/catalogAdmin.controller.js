import dogModel from "./modelosCatalogo/dog.model.js";
import breedModel from "./modelosCatalogo/breed.model.js";
import sizeModel from "./modelosCatalogo/size.model.js";
import { validarCambiosPerro } from "./dogValidation.js";

export const getPerrosAdmin = async (req, res) => {
  try {
    const perros = await dogModel.getDogs();
    return res.status(200).json({
      success: true,
      data: perros,
    });
  } catch (error) {
    return res.status(500).json({
      sucess: false,
      message: error.message || "Error interno al consultar los perros.",
    });
  }
};

// Postgres: 22P02 = id con formato inválido, 23503 = llave foránea inexistente
const ID_INVALIDO = "22P02";
const FK_INEXISTENTE = "23503";

// GET /api/catalogo/admin/perros/:id
export const getPerroAdminById = async (req, res) => {
  try {
    const perro = await dogModel.getDogAdminById(req.params.id);
    if (!perro) {
      return res.status(404).json({ success: false, message: "El perro ya no existe." });
    }
    return res.status(200).json({ success: true, data: perro });
  } catch (error) {
    if (error.code === ID_INVALIDO) {
      return res.status(404).json({ success: false, message: "El perro ya no existe." });
    }
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al consultar el perro.",
    });
  }
};

// PATCH /api/catalogo/admin/perros/:id
export const updatePerroAdmin = async (req, res) => {
  const { errores, valores } = validarCambiosPerro(req.body);
  if (Object.keys(errores).length > 0) {
    return res.status(400).json({
      success: false,
      message: "Hay campos inválidos.",
      errors: errores,
    });
  }

  try {
    const perro = await dogModel.updateDog(req.params.id, valores);
    if (!perro) {
      return res.status(404).json({ success: false, message: "El perro ya no existe." });
    }
    return res.status(200).json({ success: true, data: perro });
  } catch (error) {
    if (error.code === ID_INVALIDO) {
      return res.status(404).json({ success: false, message: "El perro ya no existe." });
    }
    if (error.code === FK_INEXISTENTE) {
      return res.status(400).json({
        success: false,
        message: "La raza o el tamaño seleccionado no existe.",
        errors: { id_raza: "Revisa la raza.", id_tamano: "Revisa el tamaño." },
      });
    }
    return res.status(500).json({
      success: false,
      message: error.message || "Error interno al actualizar el perro.",
    });
  }
};

// GET /api/catalogo/admin/razas
export const getRazasAdmin = async (req, res) => {
  try {
    return res.status(200).json({ success: true, data: await breedModel.getBreeds() });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};

// GET /api/catalogo/admin/tamanos
export const getTamanosAdmin = async (req, res) => {
  try {
    return res.status(200).json({ success: true, data: await sizeModel.getSizes() });
  } catch (error) {
    return res.status(500).json({ success: false, message: error.message });
  }
};
