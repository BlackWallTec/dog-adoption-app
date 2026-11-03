/*
Ejemplo de una ruta:
router.post("/logout", authController.postLogout);

Ahora una ruta que verifica token y rol:
router.get("/admin/perros", verificarToken, requireRole(ROLES.ADMIN,
  ROLES.ROOT), getPerrosAdmin);
*/

export default function requireRole(...roles) {
  return (req, res, next) => {
    if (!roles.includes(req.user?.rol)) {
      return res.status(403).json({
        error: "No tienes permisos para esta acción",
      });
    }
    next();
  };
}
