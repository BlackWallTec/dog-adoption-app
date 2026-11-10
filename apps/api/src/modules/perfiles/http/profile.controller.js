import "dotenv/config";
import Usuario from "./modelosPerfiles/user.model.js";

export const getProfile = async (req, _res) => {
  const _perfil = await Usuario.fetchProfileByUUID(req.user.uuid);
};
