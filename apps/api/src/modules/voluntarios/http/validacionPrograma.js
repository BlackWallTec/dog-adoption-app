// Validacion compartida entre la HU31 y HU38, crear y editar una jornada
const LIMITES = {
  nombre: 50,
  tipo: 20,
  descripcion: 1000,
  ubicacion: 100,
};
/* 
Valida y limpia los datos de una jornada
Regresa valido: true, datos } o { valido: false, campo, message}
validarInicioFuturo: false permite conservar una fecha de inicio que ya paso (al editar un programa ya iniciado)
*/
export function validarDatosPrograma(datos, { validarInicioFuturo = true } = {}) {
  if (!datos || typeof datos !== "object" || Array.isArray(datos)) {
    return { valido: false, campo: null, message: "Datos de la jornada inválidos." };
  }

  const limpios = { ...datos };

  for (const [campo, maximo] of Object.entries(LIMITES)) {
    const valor = limpios[campo];
    if (typeof valor !== "string" || !valor.trim() || valor.trim().length > maximo) {
      return { valido: false, campo, message: `El campo ${campo} es inválido.` };
    }
    limpios[campo] = valor.trim();
  }

  const inicio = new Date(`${limpios.fechaInicio} ${limpios.horaInicio}:00`);
  const fin = new Date(`${limpios.fechaFin} ${limpios.horaFin}:00`);

  if (
    Number.isNaN(inicio.getTime()) ||
    Number.isNaN(fin.getTime()) ||
    (validarInicioFuturo && inicio <= new Date()) ||
    fin <= inicio
  ) {
    return {
      valido: false,
      campo: "fechas",
      message: "Revisa las fechas y el horario de la jornada.",
    };
  }

  if (!Number.isInteger(limpios.cupoMaximo) || limpios.cupoMaximo <= 0) {
    return { valido: false, campo: "cupoMaximo", message: "El cupo debe ser un entero positivo." };
  }

  return { valido: true, datos: limpios };
}
