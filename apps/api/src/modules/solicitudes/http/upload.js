//used to process a file
import multer from "multer";

//Maximum size of the file
const maxSize = 5 * 1024 * 1024;
//allowed files to store in supabase
const permitidos = ["application/pdf", "image/jpg", "image/png"];

const recibirArchivo = multer({
  //we process the files
  storage: multer.memoryStorage(),
  limits: { fileSize: maxSize, files: 1 },
  fileFilter: (request, file, cb) => {
    if (!permitidos.includes(file.mimetype)) {
      const error = new Error("Formato no permitido. Usa PDF, JPG o PNG");
      error.status = 415;
      return cb(error);
    }
    cb(null, true);
  },
});

export function uploadDocumento(request, response, next) {
  recibirArchivo.single("archivo")(request, response, (error) => {
    if (!error) return next();
    if (error.code == "LIMIT_FILE_SIZE") {
      return response.status(413).json({ message: "El archivo supera los 5MB." });
    }
    return response.status(error.status ?? 400).json({ message: error.message });
  });
}
