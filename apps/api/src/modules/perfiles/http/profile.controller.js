import "dotenv/config";
import Usuario from "./modelosPerfiles/user.model.js";

export const getProfile = async (req, res) => {
  const perfil = await Usuario.fetchProfileByUUID(req.user.uuid);
};
