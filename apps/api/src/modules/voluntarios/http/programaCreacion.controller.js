import { randomUUID } from "node:crypto";
import * as ProgramaModel from "./modelosVoluntariado/programaCreacion.model.js";
import * as TipoProgramaModel from "./modelosVoluntariado/TipoPrograma.model.js";

// Obtiene los tipos de programas disponibles
export async function obtenerTipos(req, res) {
  try {
    const tipos = await TipoProgramaModel.obtenerTipos();
    return res.json({ tipos });
  } catch (error) {
    console.error(error);
    return res.status(500).json({
      message: "Error al cargar los tipos.",
    });
  }
}

// Registra un nuevo tipo de programa
export async function registrarTipo(req, res) {
  try {
    const { nombre, descripcion = "" } = req.body ?? {};

    if (
      typeof nombre !== "string" ||
      !nombre.trim() ||
      nombre.length > 20 ||
      typeof descripcion !== "string" ||
      descripcion.length > 200
    ) {
      return res.status(400).json({
        message: "Datos del tipo inválidos.",
      });
    }

    const tipo = await TipoProgramaModel.registrarTipo({
      nombre,
      descripcion,
    });

    return res.status(201).json({ tipo });
  } catch (error) {
    console.error(error);
    return res.status(error.status || 500).json({
      message: error.message,
    });
  }
}

// Consulta las jornadas que coinciden con un horario
export async function consultarEmpalmes(req, res) {
  try {
    const { fechaInicio, horaInicio, fechaFin, horaFin } = req.query;

    if (!fechaInicio || !horaInicio || !fechaFin || !horaFin) {
      return res.status(400).json({
        message: "Faltan fechas u horarios.",
      });
    }

    const empalmes = await ProgramaModel.obtenerEmpalmes(req.query);

    return res.json({ empalmes });
  } catch (error) {
    console.error("Error al consultar empalmes:", error);

    return res.status(500).json({
      message: "Error al consultar empalmes",
    });
  }
}

// Valida y publica una nueva jornada de voluntariado
export async function crearPrograma(req, res) {
  try {
    const datos = req.body;

    if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
      return res.status(400).json({
        message: "Datos de la jornada inválidos.",
      });
    }

    const campos = {
      nombre: 50,
      tipo: 20,
      descripcion: 1000,
      ubicacion: 100,
    };

    for (const [campo, maximo] of Object.entries(campos)) {
      if (
        typeof datos[campo] !== "string" ||
        !datos[campo].trim() ||
        datos[campo].trim().length > maximo
      ) {
        return res.status(400).json({
          message: `El campo ${campo} es inválido.`,
        });
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
      return res.status(400).json({
        message: "Revisa las fechas y el horario de la jornada.",
      });
    }

    if (!Number.isInteger(datos.cupoMaximo) || datos.cupoMaximo <= 0) {
      return res.status(400).json({
        message: "El cupo debe ser un entero positivo.",
      });
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

    return res.status(201).json({
      success: true,
      programa: resultado.programa,
    });
  } catch (error) {
    console.error("Error al guardar la jornada:", error);

    return res.status(500).json({
      message: "Error al guardar la jornada.",
      code: error.code,
    });
  }
}
