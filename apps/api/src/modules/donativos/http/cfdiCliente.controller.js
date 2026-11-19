import DonativoModel from './modelosDonativos/donativo.model.js';
import SolicitudCfdiModel from './modelosDonativos/solicitudCfdi.model.js';

const UUID_REGEX = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

//GET /api/donativos/cliente/mis-donativos (el usuario sale del token)
export const getMisDonativos = async (req, res) => {
    try {
        const donativos = await DonativoModel.getDeUsuario(req.user.uuid);
        return res.status(200).json({ success: true, data: donativos });
    } catch (error) {
        console.error(error);
        return res.status(500).json({ success: false, message: "No fue posible consultar tus donativos" });
    }
};

//POST /api/donativos/cliente/mis-donativos/:idDonativo/cfdi (HU49)
export const postSolicitudCfdi = async (req, res) => {
    const { idDonativo } = req.params;

    if (!UUID_REGEX.test(idDonativo)) {
        return res.status(400).json({ success: false, message: "Donativo inválido" });
    }

    try {
        const resultado = await SolicitudCfdiModel.crear({ idDonativo, idUsuario: req.user.uuid });

        if (resultado === "no-existe") {
            return res.status(404).json({ success: false, message: "No se encontró el donativo" });
        }

    // CA-02: sin validar no hay cdfi
    if (resultado === "no-validado") {
        return res.status(400).json({ success: false, message: "Tu donativo no esta validado" });
    }
    if (resultado === "repetida") {
        return res.status(400).json({ success: false, message: "Ya existe una solicitud de CFDI para este donativo" });
    }

    //CA -01: se crea la solicitud de CFDI
    return res.status(201).json({ success: true, message: "Solicitud de CFDI enviada" });
    } catch (error) {
        console.error("Error al solicitar el CFDI", error);
        return res.status(500).json({ success: false, message: "No fue posible solicitar el CFDI" });
    }
};

