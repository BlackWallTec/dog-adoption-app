import { Router } from "express";
import { randomUUID } from "node:crypto";
import * as ProgramaModel from "./modelosVoluntariado/programaCreacion.model.js";
import * as TipoProgramaModel from "./modelosVoluntariado/TipoPrograma.model.js";

export const router = Router();

// Solo permitir estas rutas durante el desarrollo
router.use((req, res, next) => {
  const conexionLocal = ["127.0.0.1", "::1", "::ffff:127.0.0.1"].includes(req.ip);
  const origen = req.get("Origin");
  const origenLocal = !origen || /^http:\/\/(localhost|127\.0\.0\.1):\d+$/.test(origen);
  if (process.env.NODE_ENV !== "development" || !conexionLocal || !origenLocal) {
    return res.status(403).json({ message: "Solo disponible en desarrollo local." });
  }
  next();
});

// Obtener tipos de programa
router.get("/tipos", async (req, res) => {
  try {
    const tipos = await TipoProgramaModel.obtenerTipos();
    res.json({ tipos });
  } catch (error) {
    console.error(error);
    res.status(500).json({ message: "Error al cargar los tipos." });
  }
});

// Registrar un nuevo tipo
router.post("/tipos", async (req, res) => {
  try {
    const { nombre, descripcion = "" } = req.body ?? {};
    if (
      typeof nombre !== "string" ||
      !nombre.trim() ||
      nombre.length > 20 ||
      typeof descripcion !== "string" ||
      descripcion.length > 200
    ) {
      return res.status(400).json({ message: "Datos del tipo inválidos." });
    }

    const tipo = await TipoProgramaModel.registrarTipo({ nombre, descripcion });
    res.status(201).json({ tipo });
  } catch (error) {
    console.error(error);
    res.status(error.status || 500).json({ message: error.message });
  }
});

// Consultar empalmes
router.get("/empalmes", async (req, res) => {
  try {
    const { fechaInicio, horaInicio, fechaFin, horaFin } = req.query;
    if (!fechaInicio || !horaInicio || !fechaFin || !horaFin) {
      return res.status(400).json({ message: "Falta fechas u horarios. " });
    }

    const empalmes = await ProgramaModel.obtenerEmpalmes(req.query);
    res.json({ empalmes });
  } catch (error) {
    console.error("Error al consultar empalmes:", error);
    res.status(500).json({ message: "Error al consultar empalmes" });
  }
});

// Crear jornada
router.post("/", async (req, res) => {
  try {
    const datos = req.body;
    const campos = { nombre: 50, tipo: 20, descripcion: 1000, ubicacion: 100 };

    for (const [campo, maximo] of Object.entries(campos)) {
      if (
        typeof datos[campo] !== "string" ||
        !datos[campo].trim() ||
        datos[campo].trim().length > maximo
      ) {
        return res.status(400).json({ message: `El Campo ${campo} es invalido. ` });
      }
      datos[campo] = datos[campo].trim();
    }

    const inicio = new Date(`${datos.fechaInicio} ${datos.horaInicio}:00`);
    const fin = new Date(`${datos.fechaFin} ${datos.horaFin}:00`);
    if (
      Number.isNaN(inicio.getTime()) ||
      Number.isNaN(fin.getTime()) ||
      inicio <= new Date() ||
      fin <= inicio
    ) {
      return res.status(400).json({ message: "Revisa las fechas y el horario de la jornada." });
    }

    if (!Number.isInteger(datos.cupoMaximo) || datos.cupoMaximo <= 0) {
      return res.status(400).json({ message: "El cupo debe ser un entero positivo." });
    }

    const empalmes = await ProgramaModel.obtenerEmpalmes(datos);
    if (empalmes.length > 0 && datos.confirmarEmpalme !== true) {
      return res.status(409).json({
        code: "OVERLAP",
        message: "Existen jornadas que coinciden con el horario.",
        empalmes,
      });
    }

    const idAdmin = process.env.ADMIN_ID;

    const resultado = await ProgramaModel.insertarPrograma(datos, idAdmin, randomUUID());
    res.status(201).json({ success: true, programa: resultado.programa });
  } catch (error) {
    console.error("Error al guardar la jornada:", error);
    res.status(500).json({ message: "Error al guardar la jornada.", code: error.code });
  }
});
