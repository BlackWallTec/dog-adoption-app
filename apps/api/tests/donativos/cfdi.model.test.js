import { beforeEach, describe, expect, it, vi } from "vite-plus/test";

const { supabaseAdmin, downloadMock, uploadMock, removeMock, getDatosFiscalesMock } = vi.hoisted(
  () => {
    const solicitudQuery = {
      select: vi.fn(),
      eq: vi.fn(),
      maybeSingle: vi.fn(),
    };
    const donativoQuery = {
      select: vi.fn(),
      eq: vi.fn(),
      maybeSingle: vi.fn(),
    };
    const fromMock = vi.fn();
    const downloadMock = vi.fn();
    const uploadMock = vi.fn();
    const removeMock = vi.fn();
    const getDatosFiscalesMock = vi.fn();
    const bucket = {
      download: downloadMock,
      upload: uploadMock,
      remove: removeMock,
    };
    const supabaseAdmin = {
      from: fromMock,
      storage: {
        from: vi.fn(() => bucket),
      },
    };

    solicitudQuery.select.mockReturnValue(solicitudQuery);
    solicitudQuery.eq.mockReturnValue(solicitudQuery);
    donativoQuery.select.mockReturnValue(donativoQuery);
    donativoQuery.eq.mockReturnValue(donativoQuery);
    fromMock.mockImplementation((table) =>
      table === "solicitud_cfdi" ? solicitudQuery : donativoQuery,
    );

    return { supabaseAdmin, downloadMock, uploadMock, removeMock, getDatosFiscalesMock };
  },
);

vi.mock("../../src/config/supabaseClient.js", () => ({ supabaseAdmin }));

vi.mock("../../src/modules/donativos/http/modelosDonativos/datosFiscales.model.js", () => ({
  default: { getByUsuario: getDatosFiscalesMock },
}));

import CfdiModel from "../../src/modules/donativos/http/modelosDonativos/cfdi.model.js";

const solicitudQuery = supabaseAdmin.from("solicitud_cfdi");
const donativoQuery = supabaseAdmin.from("donativo");
const idSolicitud = "b2e87183-0b85-4840-a5e7-5eae19dce391";
const solicitud = {
  id_solicitud_cfdi: idSolicitud,
  id_donativo: "donativo-1",
  cfdi_uuid: "E9BF9F1A-1445-4DE3-A372-47FA82FCBD0C",
  pdf_storage_path: `${idSolicitud}/cfdi.pdf`,
  xml_storage_path: `${idSolicitud}/cfdi.xml`,
};
const solicitudSinCfdi = {
  id_solicitud_cfdi: idSolicitud,
  id_donativo: "donativo-1",
  estado: "Pendiente de atención",
  cfdi_uuid: null,
  pdf_storage_path: null,
  xml_storage_path: null,
};

describe("CfdiModel.descargarParaCliente", () => {
  beforeEach(() => {
    vi.clearAllMocks();
    solicitudQuery.maybeSingle.mockResolvedValue({ data: solicitud, error: null });
    donativoQuery.maybeSingle.mockResolvedValue({
      data: { id_usuario: "titular-1" },
      error: null,
    });
    downloadMock.mockResolvedValue({
      data: new Blob(["%PDF-1.7"]),
      error: null,
    });
    getDatosFiscalesMock.mockResolvedValue({
      rfc: "XAXX010101000",
      correo_facturacion: "ana@example.test",
    });
  });

  it("no descarga un CFDI cuando la solicitud pertenece a otro usuario", async () => {
    const resultado = await CfdiModel.descargarParaCliente(idSolicitud, "otro-usuario", "pdf");

    expect(resultado).toBeNull();
    expect(downloadMock).not.toHaveBeenCalled();
  });

  it("descarga el archivo privado solo para el titular de la donación", async () => {
    const resultado = await CfdiModel.descargarParaCliente(idSolicitud, "titular-1", "pdf");

    expect(supabaseAdmin.storage.from).toHaveBeenCalledWith("cfdi-documentos");
    expect(downloadMock).toHaveBeenCalledWith(solicitud.pdf_storage_path);
    expect(resultado).toEqual({
      uuid: solicitud.cfdi_uuid,
      buffer: Buffer.from("%PDF-1.7"),
      contentType: "application/pdf",
    });
  });

  it("limpia el PDF privado si falla la carga del XML", async () => {
    solicitudQuery.maybeSingle
      .mockResolvedValueOnce({ data: solicitudSinCfdi, error: null })
      .mockResolvedValueOnce({ data: null, error: null });
    donativoQuery.maybeSingle.mockResolvedValue({
      data: { id_usuario: "titular-1", estado: "Validado", monto: 125 },
      error: null,
    });
    uploadMock
      .mockResolvedValueOnce({ error: null })
      .mockResolvedValueOnce({ error: { message: "Fallo de almacenamiento" } });
    removeMock.mockResolvedValue({ error: null });

    await expect(
      CfdiModel.guardarArchivos({
        idSolicitud,
        idAdministrador: "admin-1",
        datos: {
          uuid: "E9BF9F1A-1445-4DE3-A372-47FA82FCBD0C",
          rfcReceptor: "XAXX010101000",
          total: 125,
        },
        pdf: { buffer: Buffer.from("%PDF-1.7") },
        xml: { buffer: Buffer.from("<cfdi/>") },
      }),
    ).rejects.toThrow("No se pudo guardar el XML del CFDI: Fallo de almacenamiento");

    expect(removeMock).toHaveBeenCalledWith([
      `${idSolicitud}/E9BF9F1A-1445-4DE3-A372-47FA82FCBD0C.pdf`,
    ]);
  });
});
