import dogModel from "./modelosCatalogo/dog.model.js";
import multimediaModel from "./modelosCatalogo/multimedia.model.js";
import behaviourrModel from "./modelosCatalogo/behaviour.model.js";
import cuidadoEspecialModel from "./modelosCatalogo/special.model.js";

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

//Función POST Nuevo Perro
export const postNuevoPerro = async (req, res) => {
  console.log("POST /api/catalogo/admin/perros");
  let connection = await supabase.rpc("begin_transaction");
  try {
    const dogData = req.body;
    log("Datos recibidos para nuevo perro:", dogData);

    //Inicio de la transacción para crear un nuevo perro y sus relaciones

    // 1. Inserción en tabla Perro: (Incluye Raza & Tamaño)
    const perroCreado = await dogModel.createFullDog(dogData);

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
