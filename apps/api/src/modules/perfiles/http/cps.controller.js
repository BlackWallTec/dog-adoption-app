import CodigoPostal from "./modelosPerfiles/cps.model.js";
import { ERROR_CODES, errorBody } from "../../../../../shared/errores.js";

/* Recordar que este endpoint se utiliza antes de tener sesión
Regresa
200, {
  data:
      [{
        id_codigo_postal,
        codigo_postal,
        colonia,
        municipio,
        estado
      }] (o data: [] si dado código postal no existe)
    }
*/

export const getCodigosPostales = async (req, res) => {
  try {
    if (!/^\d{5}$/.test(cp)) {
      return res.status(422).json(
        errorBody(ERROR_CODES.VALIDATION, "El código postal debe tener 5 dígitos.", {
          cp: "El código postal debe tener 5 dígitos.",
        }),
      );
    }

    const data = await CodigoPostal.buscarPorCodigo(cp);
    return res.status(200).json({ data });
  } catch (error) {
    console.error(error);
    return res.status(500).json(errorBody(ERROR_CODES.INTERNAL, "Error interno del servidor"));
  }
};
