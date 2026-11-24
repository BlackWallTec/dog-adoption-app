import { describe, it, expect, vi, beforeEach } from "vite-plus/test";

const mockFrom = vi.fn();

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabaseAdmin: {
    from: (...args) => mockFrom(...args),
  },
}));

// Ajusta el nombre del archivo al final si tu función está en solicitudAdmin.repository.js o solicitudAdmin.controller.js
import { resolverSolicitud } from "../../src/modules/solicitudes/http/modelosSolicitudes/aceptarRechazar.model.js";

describe("resolverSolicitud - Resolución de solicitudes de adopción", () => {
  const catalogoEstadosMock = [
    { id_estado: 1, descripcion: "En revisión" },
    { id_estado: 2, descripcion: "Periodo de prueba" },
    { id_estado: 3, descripcion: "Aprobada" },
    { id_estado: 4, descripcion: "Rechazada" },
  ];

  let updateSolicitudMock;
  let updatePerroMock;

  beforeEach(() => {
    vi.clearAllMocks();

    updateSolicitudMock = vi.fn();
    updatePerroMock = vi.fn();

    mockFrom.mockImplementation((tabla) => {
      if (tabla === "estado_solicitud") {
        return {
          select: vi.fn().mockResolvedValue({
            data: catalogoEstadosMock,
            error: null,
          }),
        };
      }

      if (tabla === "solicitud_adopcion") {
        return {
          update: (campos) => {
            updateSolicitudMock(campos);
            return {
              eq: () => ({
                select: () => ({
                  single: vi.fn().mockResolvedValue({
                    data: {
                      id_solicitud: "sol-123",
                      id_perro: "perro-99",
                      ...campos,
                    },
                    error: null,
                  }),
                }),
              }),
            };
          },
        };
      }

      if (tabla === "perro") {
        return {
          update: (campos) => {
            updatePerroMock(campos);
            return {
              eq: vi.fn().mockResolvedValue({ error: null }),
            };
          },
        };
      }
    });
  });

  it("debe aprobar la solicitud, limpiar comentario_admin, asignar fecha_resolucion y marcar el perro como Adoptado", async () => {
    const resultado = await resolverSolicitud("sol-123", "aprobada", "Texto que debe ignorarse");

    expect(updateSolicitudMock).toHaveBeenCalledWith({
      id_estado: 3,
      comentario_admin: null,
      fecha_resolucion: expect.any(String),
      puede_editar: false,
    });

    expect(updatePerroMock).toHaveBeenCalledWith({ estado: "Adoptado" });
    expect(resultado.id_estado).toBe(3);
  });

  it("debe rechazar la solicitud, guardar comentario_admin, asignar fecha_resolucion y NO modificar el estado del perro", async () => {
    const motivo = "No cuenta con espacio cerrado en patio.";
    const resultado = await resolverSolicitud("sol-123", "rechazada", motivo);

    expect(updateSolicitudMock).toHaveBeenCalledWith({
      id_estado: 4,
      comentario_admin: motivo,
      fecha_resolucion: expect.any(String),
      puede_editar: false,
    });

    expect(updatePerroMock).not.toHaveBeenCalled();
    expect(resultado.comentario_admin).toBe(motivo);
  });

  it("debe pasar a periodo_prueba dejando fecha_resolucion y comentario_admin en null", async () => {
    await resolverSolicitud("sol-123", "periodo_prueba", null);

    expect(updateSolicitudMock).toHaveBeenCalledWith({
      id_estado: 2,
      comentario_admin: null,
      fecha_resolucion: null,
      puede_editar: false,
    });

    expect(updatePerroMock).not.toHaveBeenCalled();
  });

  it("debe pasar a en_revision normalizando acentos ('En revisión') y dejando fecha_resolucion en null", async () => {
    await resolverSolicitud("sol-123", "en_revision", null);

    expect(updateSolicitudMock).toHaveBeenCalledWith({
      id_estado: 1,
      comentario_admin: null,
      fecha_resolucion: null,
      puede_editar: false,
    });
  });

  it("debe rechazar dejando la solicitud editable cuando se manda puedeEditar = true", async () => {
    const motivo = "Falta comprobante de domicilio vigente.";
    const resultado = await resolverSolicitud("sol-123", "rechazada", motivo, true);

    expect(updateSolicitudMock).toHaveBeenCalledWith({
      id_estado: 4,
      comentario_admin: motivo,
      fecha_resolucion: expect.any(String),
      puede_editar: true,
    });
    expect(resultado.puede_editar).toBe(true);
  });

  it("debe dejar puede_editar en false al rechazar si no se manda el permiso", async () => {
    await resolverSolicitud("sol-123", "rechazada", "Motivo");

    expect(updateSolicitudMock).toHaveBeenCalledWith(
      expect.objectContaining({ puede_editar: false }),
    );
  });

  it("debe forzar puede_editar en false si el estado no es rechazada, aunque se mande true", async () => {
    await resolverSolicitud("sol-123", "aprobada", null, true);

    expect(updateSolicitudMock).toHaveBeenCalledWith(
      expect.objectContaining({ puede_editar: false }),
    );
  });

  it("debe lanzar un error si el estado recibido no existe en el catálogo", async () => {
    await expect(resolverSolicitud("sol-123", "estado_inexistente", null)).rejects.toThrow(
      'No se encontró un estado válido en estado_solicitud para "estado_inexistente"',
    );
  });

  it("debe propagar el error si falla la consulta a estado_solicitud", async () => {
    mockFrom.mockImplementationOnce(() => ({
      select: vi.fn().mockResolvedValue({
        data: null,
        error: new Error("Error de conexión con Supabase"),
      }),
    }));

    await expect(resolverSolicitud("sol-123", "aprobada", null)).rejects.toThrow(
      "Error de conexión con Supabase",
    );
  });
});
