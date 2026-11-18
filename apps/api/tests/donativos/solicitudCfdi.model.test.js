import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { fromMock, crearUrlFirmadaMock } = vi.hoisted(() => ({
  fromMock: vi.fn(),
  crearUrlFirmadaMock: vi.fn().mockResolvedValue("https://storage.example/constancia"),
}));

vi.mock("../../src/config/supabaseClient.js", () => ({
  supabaseAdmin: { from: fromMock },
}));

vi.mock("../../src/modules/donativos/http/modelosDonativos/datosFiscales.model.js", () => ({
  default: {
    getByUsuario: vi.fn(),
    esRutaConstanciaValida: vi.fn(
      (idUsuario, ruta) => typeof ruta === "string" && ruta === `${idUsuario}/constancia.pdf`,
    ),
    crearUrlFirmadaConstancia: crearUrlFirmadaMock,
  },
}));

import DatosFiscalesModel from "../../src/modules/donativos/http/modelosDonativos/datosFiscales.model.js";
import SolicitudCfdiModel from "../../src/modules/donativos/http/modelosDonativos/solicitudCfdi.model.js";

function crearQuery(resultado) {
  return {
    select: vi.fn().mockReturnThis(),
    eq: vi.fn().mockReturnThis(),
    order: vi.fn().mockResolvedValue(resultado),
    in: vi.fn().mockResolvedValue(resultado),
    maybeSingle: vi.fn().mockResolvedValue(resultado),
  };
}

describe("SolicitudCfdiModel", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("filtra las solicitudes pendientes y omite datos fiscales de la bandeja", async () => {
    const solicitudesQuery = crearQuery({
      data: [
        {
          id_solicitud_cfdi: "b2e87183-0b85-4840-a5e7-5eae19dce391",
          id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
          estado: "Pendiente de atención",
          fecha_solicitud: "2026-11-14T10:00:00Z",
        },
      ],
      error: null,
    });
    const donativosQuery = crearQuery({
      data: [
        {
          id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
          estado: "Validado",
          fecha: "2026-11-13T10:00:00Z",
          usuario: { id_usuario: "user-1", nombre: "Ana", apellidos: "Luna" },
        },
      ],
      error: null,
    });
    const perfilesQuery = crearQuery({
      data: [
        {
          id_usuario: "user-1",
          rfc: "XAXX010101000",
          razon_social: "Ana Luna",
          regimen_fiscal: "601",
          codigo_postal: "76000",
          correo_facturacion: "facturacion@example.com",
          constancia_url: "user-1/constancia.pdf",
        },
      ],
      error: null,
    });
    fromMock.mockImplementation((table) => {
      if (table === "solicitud_cfdi") return solicitudesQuery;
      if (table === "donativo") return donativosQuery;
      if (table === "datos_confidenciales") return perfilesQuery;
      throw new Error(`Tabla inesperada: ${table}`);
    });

    const solicitudes = await SolicitudCfdiModel.getPendientes();

    expect(solicitudesQuery.eq).toHaveBeenCalledWith("estado", "Pendiente de atención");
    expect(solicitudesQuery.order).toHaveBeenCalledWith("fecha_solicitud", { ascending: true });
    expect(solicitudes).toEqual([
      {
        id_solicitud_cfdi: "b2e87183-0b85-4840-a5e7-5eae19dce391",
        id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
        fecha_solicitud: "2026-11-14T10:00:00Z",
        estado: "Pendiente de atención",
        donante: "Ana Luna",
        requiere_revision: false,
      },
    ]);
    expect(solicitudes[0]).not.toHaveProperty("datos_fiscales");
    expect(solicitudes[0]).not.toHaveProperty("rfc");
  });

  it("marca la solicitud para revisión cuando faltan datos fiscales", async () => {
    fromMock.mockImplementation((table) => {
      if (table === "solicitud_cfdi") {
        return crearQuery({
          data: [
            {
              id_solicitud_cfdi: "b2e87183-0b85-4840-a5e7-5eae19dce391",
              id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
              estado: "Pendiente de atención",
              fecha_solicitud: "2026-11-14T10:00:00Z",
            },
          ],
          error: null,
        });
      }
      if (table === "donativo") {
        return crearQuery({
          data: [
            {
              id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
              estado: "Validado",
              usuario: { id_usuario: "user-1", nombre: "Ana", apellidos: "Luna" },
            },
          ],
          error: null,
        });
      }
      if (table === "datos_confidenciales") return crearQuery({ data: [], error: null });
      throw new Error(`Tabla inesperada: ${table}`);
    });

    const [solicitud] = await SolicitudCfdiModel.getPendientes();

    expect(solicitud.requiere_revision).toBe(true);
  });

  it("devuelve datos fiscales y un enlace temporal solo en el detalle", async () => {
    const idSolicitud = "b2e87183-0b85-4840-a5e7-5eae19dce391";
    fromMock.mockImplementation((table) => {
      if (table === "solicitud_cfdi") {
        return crearQuery({
          data: {
            id_solicitud_cfdi: idSolicitud,
            id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
            estado: "Pendiente de atención",
            fecha_solicitud: "2026-11-14T10:00:00Z",
          },
          error: null,
        });
      }
      if (table === "donativo") {
        return crearQuery({
          data: {
            id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
            id_usuario: "user-1",
            monto: 250,
            fecha: "2026-11-13T10:00:00Z",
            estado: "Validado",
            usuario: { id_usuario: "user-1", nombre: "Ana", apellidos: "Luna" },
          },
          error: null,
        });
      }
      throw new Error(`Tabla inesperada: ${table}`);
    });
    DatosFiscalesModel.getByUsuario.mockResolvedValue({
      rfc: "XAXX010101000",
      razon_social: "Ana Luna",
      regimen_fiscal: "601",
      codigo_postal: "76000",
      correo_facturacion: "facturacion@example.com",
      constancia_url: "user-1/constancia.pdf",
    });

    const detalle = await SolicitudCfdiModel.getDetallePendiente(idSolicitud);

    expect(detalle.listo_para_emitir).toBe(true);
    expect(detalle.datos_fiscales).toEqual({
      rfc: "XAXX010101000",
      razon_social: "Ana Luna",
      regimen_fiscal: "601",
      codigo_postal: "76000",
      correo_facturacion: "facturacion@example.com",
      constancia_url: "https://storage.example/constancia",
    });
    expect(crearUrlFirmadaMock).toHaveBeenCalledWith("user-1", "user-1/constancia.pdf");
  });

  it("mantiene el detalle si falla el enlace firmado y lo señala para revisión", async () => {
    const idSolicitud = "b2e87183-0b85-4840-a5e7-5eae19dce391";
    fromMock.mockImplementation((table) => {
      if (table === "solicitud_cfdi") {
        return crearQuery({
          data: {
            id_solicitud_cfdi: idSolicitud,
            id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
            estado: "Pendiente de atención",
            fecha_solicitud: "2026-11-14T10:00:00Z",
          },
          error: null,
        });
      }
      if (table === "donativo") {
        return crearQuery({
          data: {
            id_donativo: "f5348be0-4b19-4bf8-b72b-5721b51c5e93",
            id_usuario: "user-1",
            monto: 250,
            fecha: "2026-11-13T10:00:00Z",
            estado: "Validado",
            usuario: { id_usuario: "user-1", nombre: "Ana", apellidos: "Luna" },
          },
          error: null,
        });
      }
      throw new Error(`Tabla inesperada: ${table}`);
    });
    DatosFiscalesModel.getByUsuario.mockResolvedValue({
      rfc: "XAXX010101000",
      razon_social: "Ana Luna",
      regimen_fiscal: "601",
      codigo_postal: "76000",
      correo_facturacion: "facturacion@example.com",
      constancia_url: "user-1/constancia.pdf",
    });
    crearUrlFirmadaMock.mockRejectedValueOnce(new Error("Archivo no encontrado"));

    const detalle = await SolicitudCfdiModel.getDetallePendiente(idSolicitud);

    expect(detalle.datos_fiscales.constancia_url).toBeNull();
    expect(detalle.listo_para_emitir).toBe(false);
    expect(detalle.motivos_revision).toContain(
      "No se pudo generar el acceso temporal a la constancia fiscal.",
    );
  });
});
