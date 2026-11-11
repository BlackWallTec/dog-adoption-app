import "dotenv/config";
import Usuario from "./modelosPerfiles/user.model.js";
import { ERROR_CODES, errorBody, UNAUTHENTICATED } from "../../../../../shared/errores.js";

export const getProfile = async (req, res) => {
  try {
    // El uuid sale del token (req.user), nunca de query ni params
    const profile = await Usuario.fetchProfileByUUID(req.user.uuid);

    if (!profile) throw new UNAUTHENTICATED();

    return res.status(200).json({ profile });
  } catch (error) {
    if (error instanceof UNAUTHENTICATED) {
      return res.status(401).json(errorBody(ERROR_CODES.UNAUTHENTICATED, error.message));
    }

    console.error(error);
    return res.status(500).json(errorBody(ERROR_CODES.INTERNAL, "Error interno del servidor"));
  }
};
