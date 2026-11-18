// utils/getTokenFromReq.js
// Esta funcion se encarga únicamente de leer el token de la petición HTTP
// (soportando tanto Web con cookies como Móvil con Bearer)

export const getTokenFromReq = (req) => {
  // Si llegamos a agregar req.token, lo toma directo.
  if (req.token) return req.token;

  const esMovil = req.get("x-client") === "mobile";
  return esMovil ? req.get("authorization")?.match(/^Bearer\s+(.+)$/i)?.[1] : req.cookies?.token;
};
