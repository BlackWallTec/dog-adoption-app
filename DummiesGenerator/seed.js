// ============================================================================
// QARIÑO ANIMAL - SCRIPT DE POBLADO DE DATOS (SEEDING)
// ============================================================================
// Actualizado contra ScriptBD.sql (v1.1 - 15/Oct/2026).
// Genera dummies con integridad referencial total, capa por capa.
// Ejecución en terminal: node seed.js
// ============================================================================

import { createClient } from '@supabase/supabase-js';
import { fakerES_MX as faker } from '@faker-js/faker';
import 'dotenv/config';

import readline from 'node:readline/promises';
import { stdin as input, stdout as output } from 'node:process';
import { randomUUID } from 'node:crypto';

// ============================================================================
// Sección de Borrado Inicial (sin cambios respecto a tu versión)
// ============================================================================
async function preguntarLimpieza() {
  const terminal = readline.createInterface({ input, output });

  try {
    const respuesta = await terminal.question(
      '¿Deseas borrar los datos existentes en Supabase antes del seeding? (s/n): '
    );

    if (respuesta.trim().toLowerCase() !== 's') {
      console.log('ℹ️ Se conservarán los datos existentes.');
      return;
    }

    const confirmacion = await terminal.question(
      '⚠️ Esta acción eliminará datos. Escribe BORRAR para confirmar: '
    );

    if (confirmacion.trim() !== 'BORRAR') {
      console.log('🛑 Limpieza cancelada. Se conservarán los datos.');
      return;
    }

    console.log('🧹 Limpiando tablas de prueba...');

    const { data, error } = await supabase.rpc('limpiar_datos_prueba');

    if (error) {
      throw new Error(`Error durante la limpieza: ${error.message}`);
    }

    console.log(`✅: ${data?.message || 'Limpieza completada con éxito.'}`);
  } finally {
    terminal.close();
  }
}

// Inicialización del cliente de Supabase usando Service Role (Bypass de políticas RLS)
const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);

/**
 * UTILERÍA: Generador de IDs.
 * En el modelo actual todas las PK son UUID (DEFAULT gen_random_uuid()), así que
 * generamos el UUID en el cliente y lo mandamos explícito en el insert. Esto nos
 * permite usar el mismo ID para amarrar las FK de capas siguientes sin tener que
 * leer de vuelta lo que Postgres generó. El "prefijo" ya no se guarda en la BD,
 * es solo para que el código/los logs sean legibles (ver tabla de prefijos al final).
 *
 * @param {string} prefijo - Prefijo corto solo para contexto en logs (ej: 'usr', 'dog').
 * @returns {string} UUID v4 válido.
 */
function generarId(prefijo) {
  return randomUUID();
}

/**
 * UTILERÍA: Insert con manejo de errores centralizado.
 * Usa .select() para devolver las filas tal como quedaron en la BD, así las
 * capas siguientes pueden encadenar FKs sin adivinar qué se insertó.
 *
 * @param {string} tabla - Nombre real de la tabla en Postgres (todo minúsculas).
 * @param {object|object[]} filas - Fila o arreglo de filas a insertar.
 * @param {string} [etiqueta] - Nombre amigable para el log de error.
 * @returns {Promise<object[]>} Filas insertadas (arreglo vacío si falló).
 */
async function insertar(tabla, filas, etiqueta) {
  const { data, error } = await supabase.from(tabla).insert(filas).select();
  if (error) {
    console.error(`❌ Error en ${etiqueta || tabla}:`, error.message);
    return [];
  }
  return data ?? [];
}

/** Devuelve una fecha reciente/pasada ya formateada para columnas TIMESTAMP. */
function fechaISO(date) {
  return date.toISOString();
}

// ============================================================================
// FASE 1: CATÁLOGOS MAESTROS (TABLAS INDEPENDIENTES)
// ============================================================================

/** Puebla 'rol'. PK = Nombre. */
async function poblarRoles() {
  console.log('🔹 Insertando catálogo de Roles...');
  const roles = [
    { nombre: 'Administrador', descripcion: 'Control total del refugio y aprobaciones', activo: true },
    { nombre: 'Cliente', descripcion: 'Usuario interesado en adoptar o donar', activo: true },
  ];
  await insertar('rol', roles, 'Catálogo Roles');
  return roles.map((r) => r.nombre);
}

/** Puebla 'privilegio'. PK = Privilegio. */
async function poblarPrivilegios() {
  console.log('🔹 Insertando catálogo de Privilegios...');
  const privilegios = [
    { privilegio: 'Puede subir fotos' },
    { privilegio: 'Puede aprobar solicitudes' },
    { privilegio: 'Puede registrar donativos' },
    { privilegio: 'Inscripción a programas' },
    { privilegio: 'Puede gestionar usuarios' },
  ];
  await insertar('privilegio', privilegios, 'Catálogo Privilegios');
  return privilegios.map((p) => p.privilegio);
}

/**
 * Puebla el pivote 'rol_tiene_privilegio' (TABLA NUEVA).
 * Regla de negocio: el Administrador tiene todos los privilegios;
 * el Cliente solo los que le aplican a su rol de autoservicio.
 */
async function poblarRolTienePrivilegio(nombresRoles, nombresPrivilegios) {
  console.log('🔹 Insertando catálogo de Rol_tiene_Privilegio...');

  const privilegiosCliente = nombresPrivilegios.filter((p) =>
    ['Puede registrar donativos', 'Inscripción a programas'].includes(p)
  );

  const filas = [
    ...nombresPrivilegios.map((p) => ({ privilegio: p, nombre_rol: 'Administrador' })),
    ...privilegiosCliente.map((p) => ({ privilegio: p, nombre_rol: 'Cliente' })),
  ];

  await insertar('rol_tiene_privilegio', filas, 'Rol_tiene_Privilegio');
}

/** Puebla 'raza'. */
async function poblarRazas() {
  console.log('🔹 Insertando catálogo de Razas...');
  const razasBase = [
    { nombre: 'Mestizo', descripcion: 'Cruza única e invaluable' },
    { nombre: 'Pastor Alemán', descripcion: 'Perro guardián, inteligente y leal' },
    { nombre: 'Golden Retriever', descripcion: 'Amigable, juguetón y excelente con niños' },
    { nombre: 'Chihuahua', descripcion: 'Pequeño de tamaño pero con gran personalidad' },
    { nombre: 'Labrador', descripcion: 'Activo, sociable y fácil de entrenar' },
    { nombre: 'Schnauzer', descripcion: 'Enérgico, fiel y poco alérgeno' },
  ];
  const registros = razasBase.map((raza) => ({
    id_raza: generarId('raz'),
    nombre: raza.nombre,
    descripcion: raza.descripcion,
  }));
  return insertar('raza', registros, 'Catálogo Razas');
}

/** Puebla 'tamano'. */
async function poblarTamanos() {
  console.log('🔹 Insertando catálogo de Tamaños...');
  const tamanosBase = [
    { nombre: 'Chico', descripcion: 'Menor a 45cm de altura' },
    { nombre: 'Mediano', descripcion: 'De 45 a 75cm de altura' },
    { nombre: 'Grande', descripcion: 'Mayor a 75cm de altura' },
  ];
  const registros = tamanosBase.map((tam) => ({
    id_tamano: generarId('tam'),
    nombre: tam.nombre,
    descripcion: tam.descripcion,
  }));
  return insertar('tamano', registros, 'Catálogo Tamaños');
}

/** Puebla 'caracter'. */
async function poblarCaracteres() {
  console.log('🔹 Insertando catálogo de Caracteres...');
  const caracteresBase = [
    { nombre: 'Juguetón', descripcion: 'Mucha energía, ideal para familias activas' },
    { nombre: 'Tranquilo', descripcion: 'Poco ruidoso, ideal para departamentos' },
    { nombre: 'Amigable con niños', descripcion: 'Súper paciente y dócil con infantes' },
    { nombre: 'Protector', descripcion: 'Alerta a su entorno y territorial' },
    { nombre: 'Independiente', descripcion: 'No requiere atención constante' },
  ];
  const registros = caracteresBase.map((car) => ({
    id_caracter: generarId('car'),
    nombre: car.nombre,
    descripcion: car.descripcion,
  }));
  return insertar('caracter', registros, 'Catálogo Caracteres');
}

/** Puebla 'cuidado_especial'. */
async function poblarCuidados() {
  console.log('🔹 Insertando catálogo de Cuidados Especiales...');
  const cuidadosBase = [
    'Dieta estricta libre de pollo',
    'Medicamento diario para articulaciones',
    'Limpieza constante de orejas por propensión a otitis',
    'Ninguno (Salud óptima)',
    'Requiere control de peso',
  ];
  const registros = cuidadosBase.map((desc) => ({
    id_cuidado: generarId('cui'),
    descripcion: desc,
  }));
  return insertar('cuidado_especial', registros, 'Catálogo Cuidados');
}

/** Puebla 'estado_solicitud'. */
async function poblarEstadosSolicitud() {
  console.log('🔹 Insertando catálogo de Estados de Solicitud...');
  const estados = ['Pendiente de Revisión', 'En Periodo de Prueba', 'Aprobada Definitiva', 'Rechazada'];
  const registros = estados.map((desc) => ({
    id_estado: generarId('est'),
    descripcion: desc,
  }));
  return insertar('estado_solicitud', registros, 'Catálogo Estados');
}

/** Puebla 'tipo_programa'. PK = Nombre. */
async function poblarTiposPrograma() {
  console.log('🔹 Insertando catálogo de Tipos de Programa...');
  const tipos = [
    { nombre: 'Recaudación Monetaria', descripcion: 'Eventos masivos para recolectar fondos de ayuda' },
    { nombre: 'Cuidado de Perros', descripcion: 'El voluntariado ayuda directamente con el baño y paseo' },
    { nombre: 'Pasarela de Adopción', descripcion: 'Eventos públicos para mostrar perritos disponibles' },
    { nombre: 'Educación y Difusión', descripcion: 'Pláticas sobre tenencia responsable' },
  ];
  await insertar('tipo_programa', tipos, 'Catálogo Tipos Programa');
  return tipos.map((t) => t.nombre);
}

/** Puebla 'catalogo_de_cps'. */
async function poblarCPs() {
  console.log('🔹 Insertando catálogo de Códigos Postales...');
  const base = [
    { codigo_postal: '76000', colonia: 'Centro Histórico', municipio: 'Querétaro', estado: 'Querétaro' },
    { codigo_postal: '76130', colonia: 'Villas del Sol', municipio: 'Querétaro', estado: 'Querétaro' },
    { codigo_postal: '01000', colonia: 'San Ángel', municipio: 'Álvaro Obregón', estado: 'CDMX' },
    { codigo_postal: '06700', colonia: 'Roma Norte', municipio: 'Cuauhtémoc', estado: 'CDMX' },
    { codigo_postal: '44100', colonia: 'Centro', municipio: 'Guadalajara', estado: 'Jalisco' },
    { codigo_postal: '64000', colonia: 'Centro', municipio: 'Monterrey', estado: 'Nuevo León' },
    { codigo_postal: '72000', colonia: 'Centro', municipio: 'Puebla', estado: 'Puebla' },
    { codigo_postal: '20000', colonia: 'Centro', municipio: 'Aguascalientes', estado: 'Aguascalientes' },
    { codigo_postal: '37000', colonia: 'Centro', municipio: 'León', estado: 'Guanajuato' },
    { codigo_postal: '91000', colonia: 'Centro', municipio: 'Xalapa', estado: 'Veracruz' },
  ];
  const registros = base.map((cp) => ({
    id_codigo_postal: generarId('cp'),
    ...cp,
    pais: 'México',
  }));
  return insertar('catalogo_de_cps', registros, 'Catálogo CPs');
}

/** Puebla 'mensaje'. */
async function poblarMensajes() {
  console.log('🔹 Insertando catálogo de Mensajes del sistema...');
  const base = [
    { titulo: 'Cita de Adopción', texto: 'Le recordamos su cita mañana a las 10:00 AM.', tipo: 'Recordatorio' },
    { titulo: 'Donativo Recibido', texto: 'Muchas gracias por tu apoyo monetario.', tipo: 'Agradecimiento' },
    { titulo: 'Documentos Aprobados', texto: 'Tu documentación fue validada con éxito.', tipo: 'Notificación' },
    { titulo: 'Solicitud en Revisión', texto: 'Tu solicitud de adopción está siendo revisada.', tipo: 'Notificación' },
    { titulo: 'Periodo de Prueba Iniciado', texto: 'Ya puedes llevarte a tu nuevo compañero a casa.', tipo: 'Notificación' },
    { titulo: 'Nuevo Programa Disponible', texto: 'Hay un nuevo programa de voluntariado abierto.', tipo: 'Difusión' },
    { titulo: 'Recordatorio de Seguimiento', texto: 'Un admin te visitará esta semana.', tipo: 'Recordatorio' },
    { titulo: 'Bienvenida', texto: 'Gracias por registrarte en Qariño Animal.', tipo: 'Notificación' },
  ];
  const registros = base.map((m) => ({ id_mensaje: generarId('msg'), ...m }));
  return insertar('mensaje', registros, 'Catálogo Mensajes');
}

// ============================================================================
// FASE 2: USUARIOS, SUBTIPOS, DATOS CONFIDENCIALES Y DIRECCIONES
// ============================================================================

/**
 * FUNCIÓN CORE: Genera Usuarios (Admins y Clientes) y sus entidades dependientes.
 * - usuario -> admin | cliente (herencia 1:1, misma PK)
 * - cliente -> datos_confidenciales (el correo y teléfono viven aquí, no en usuario)
 * - usuario (cualquiera) -> direcciones_fisicas (1 a 2 por usuario)
 *
 * @param {Array} cpsDisponibles - Catálogo de CPs de la Fase 1.
 */
async function generarUsuarios(cpsDisponibles) {
  console.log('⚙️ Iniciando generación masiva de Usuarios y relaciones directas...');

  const listaClientes = [];
  const listaAdmins = [];
  const listaTodosUsuarios = [];

  const TOTAL_ADMINS = 5;
  const TOTAL_CLIENTES = 20;
  const total = TOTAL_ADMINS + TOTAL_CLIENTES;

  for (let i = 0; i < total; i++) {
    const esAdmin = i < TOTAL_ADMINS;
    const rolElegido = esAdmin ? 'Administrador' : 'Cliente';
    const idUsuarioNuevo = generarId('usr');

    // 1. REGISTRO PADRE (Tabla: usuario) — ya no lleva correo/telefono, eso se fue a Datos_confidenciales
    const usuarioDummie = {
      id_usuario: idUsuarioNuevo,
      rol: rolElegido, // FK -> rol(nombre)
      nombre: faker.person.firstName(),
      apellidos: faker.person.lastName(),
      contrasena: '$2b$12$eImiTXuWVxfM37uY4JANje...', // Hash estático simulado
      fecha_nacimiento: fechaISO(faker.date.birthdate({ min: 18, max: 65, mode: 'age' })),
      fecha_registro: fechaISO(faker.date.past({ years: 1 })),
    };

    const [insertado] = await insertar('usuario', usuarioDummie, 'Usuario base');
    if (!insertado) continue; // Si falla el padre, no generamos huérfanos

    listaTodosUsuarios.push(idUsuarioNuevo);

    // 2. HERENCIA 1:1 (CASO: Subtipos con la misma PK)
    if (esAdmin) {
      await insertar('admin', { id_usuario: idUsuarioNuevo }, 'Admin');
      listaAdmins.push(idUsuarioNuevo);
      console.log(`👤 Admin registrado: ${usuarioDummie.nombre} (${idUsuarioNuevo})`);
    } else {
      await insertar('cliente', { id_usuario: idUsuarioNuevo }, 'Cliente');
      listaClientes.push(idUsuarioNuevo);
      console.log(`👥 Cliente registrado: ${usuarioDummie.nombre} (${idUsuarioNuevo})`);

      // 3. DATOS CONFIDENCIALES (TABLA NUEVA) — FK apunta a Cliente, no a Usuario en general
      const cpParaDatos = faker.helpers.arrayElement(cpsDisponibles);
      const datosConfidencialesDummie = {
        id_usuario: idUsuarioNuevo,
        rfc: faker.string.alphanumeric(13).toUpperCase(),
        razon_social: faker.company.name(),
        regimen_fiscal: faker.helpers.arrayElement(['601', '603', '612', '621', '626']),
        codigo_postal: cpParaDatos.codigo_postal,
        correo: faker.internet.email({ firstName: usuarioDummie.nombre }),
        correo_facturacion: faker.internet.email(),
        telefono: faker.phone.number({ style: 'national' }),
        constancia_url: `https://qarinoanimal.com/constancias/${faker.string.alphanumeric(10)}.pdf`,
      };
      await insertar('datos_confidenciales', datosConfidencialesDummie, 'Datos_confidenciales');
    }

    // 4. DIRECCIONES FÍSICAS (1:N) — ahora aplica a CUALQUIER usuario, no solo clientes
    const cantidadDirecciones = faker.datatype.boolean({ probability: 0.25 }) ? 2 : 1;
    for (let d = 0; d < cantidadDirecciones; d++) {
      const cpAleatorio = faker.helpers.arrayElement(cpsDisponibles);
      const direccionDummie = {
        id_direccion: generarId('dir'),
        id_usuario: idUsuarioNuevo,
        id_codigo_postal: cpAleatorio.id_codigo_postal,
        calle: faker.location.street(),
        numero_exterior: faker.number.int({ min: 1, max: 999 }),
        numero_interior: faker.datatype.boolean() ? faker.number.int({ min: 1, max: 20 }) : null,
      };
      await insertar('direcciones_fisicas', direccionDummie, 'Direcciones_Fisicas');
    }
  }

  return { clientes: listaClientes, admins: listaAdmins, todos: listaTodosUsuarios };
}

// ============================================================================
// FASE 3: NOTIFICACIONES (PIVOTE M:N Cliente <-> Mensaje)
// ============================================================================

/**
 * Puebla 'notifica'. Nota: la columna es id_cliente (no id_usuario), pero el
 * valor es el mismo id_usuario del cliente porque comparten PK.
 */
async function generarNotificaciones(clientesIds, mensajesDisponibles) {
  console.log('⚙️ Iniciando generación de Notificaciones...');

  for (const idCliente of clientesIds) {
    const cantidadMensajes = faker.number.int({ min: 1, max: 2 });
    const mensajesElegidos = faker.helpers.arrayElements(mensajesDisponibles, cantidadMensajes);

    for (const mensaje of mensajesElegidos) {
      await insertar(
        'notifica',
        {
          id_cliente: idCliente,
          id_mensaje: mensaje.id_mensaje,
          fecha_envio: fechaISO(faker.date.recent({ days: 30 })),
          leido: faker.datatype.boolean(),
        },
        'Notifica'
      );
    }
  }
}

// ============================================================================
// FASE 4: MÓDULO CANINO (PERROS, MULTIMEDIA Y PIVOTES)
// ============================================================================

/**
 * FUNCIÓN CORE: Genera Perros y su ecosistema.
 * OJO: Perro ya NO tiene id_caracter propio — el carácter se amarra solo vía
 * el pivote perro_tiene_caracter (puede tener varios).
 */
async function generarPerrosYEcosistema(razasDisponibles, tamanosDisponibles, caracteresDisponibles, cuidadosDisponibles) {
  console.log('⚙️ Iniciando generación masiva de Perritos y su ecosistema...');

  const listaPerrosCreados = [];
  const TOTAL_PERROS = 20;

  for (let i = 0; i < TOTAL_PERROS; i++) {
    const idPerroNuevo = generarId('dog');

    const razaAleatoria = faker.helpers.arrayElement(razasDisponibles);
    const tamanoAleatorio = faker.helpers.arrayElement(tamanosDisponibles);

    const perroDummie = {
      id_perro: idPerroNuevo,
      id_raza: razaAleatoria.id_raza,
      id_tamano: tamanoAleatorio.id_tamano,
      nombre: faker.person.firstName(),
      sexo: faker.helpers.arrayElement(['Macho', 'Hembra']),
      edad_aprox: faker.number.int({ min: 1, max: 14 }),
      tamano_medida: faker.number.float({ min: 20, max: 90, fractionDigits: 1 }),
      descripcion: 'Un perrito rescatado, muy noble, ideal para llenar tu hogar de amor.',
      esterilizado: faker.datatype.boolean(),
      vacunado: faker.datatype.boolean(),
      estado: faker.helpers.arrayElement(['Disponible', 'En proceso de adopción', 'Adoptado']),
      fecha_ingreso: fechaISO(faker.date.past({ years: 2 })),
    };

    const [insertado] = await insertar('perro', perroDummie, 'Perro');
    if (!insertado) continue;

    listaPerrosCreados.push(idPerroNuevo);
    console.log(`🐾 Perrito registrado: ${perroDummie.nombre} (${idPerroNuevo})`);

    // PIVOTE: perro_tiene_caracter — 2 caracteres distintos por perro
    const caracteresElegidos = faker.helpers.arrayElements(caracteresDisponibles, 2);
    for (const car of caracteresElegidos) {
      await insertar(
        'perro_tiene_caracter',
        { id_perro: idPerroNuevo, id_caracter: car.id_caracter },
        'Perro_tiene_Caracter'
      );
    }

    // PIVOTE: perro_tiene_cuidado_especial — 1 cuidado por perro
    const cuidadoAleatorio = faker.helpers.arrayElement(cuidadosDisponibles);
    await insertar(
      'perro_tiene_cuidado_especial',
      { id_perro: idPerroNuevo, id_cuidado: cuidadoAleatorio.id_cuidado },
      'Perro_tiene_Cuidado_Especial'
    );

    // GALERÍA: multimedia — 2 fotos reales por perro (picsum, cargan de verdad)
    for (let m = 0; m < 2; m++) {
      const multimediaDummie = {
        id_multimedia: generarId('mul'),
        id_perro: idPerroNuevo,
        url: `https://picsum.photos/seed/${faker.string.alphanumeric(8)}/640/480`,
        tipo: 'Foto',
        fecha_subida: fechaISO(faker.date.recent({ days: 15 })),
      };
      await insertar('multimedia', multimediaDummie, 'Multimedia');
    }
  }

  return listaPerrosCreados;
}

// ============================================================================
// FASE 5: DOCUMENTOS Y SOLICITUDES DE ADOPCIÓN
// ============================================================================

/** Genera 1-2 documentos por cliente. Devuelve [{idDoc, idCliente}]. */
async function generarDocumentos(clientesIds) {
  console.log('⚙️ Iniciando generación de Documentos...');
  const listaDocumentosCreados = [];

  for (const idCliente of clientesIds) {
    const cantidadDocs = faker.number.int({ min: 1, max: 2 });
    for (let d = 0; d < cantidadDocs; d++) {
      const idDocNuevo = generarId('doc');
      const documentoDummie = {
        id_documento: idDocNuevo,
        id_usuario: idCliente,
        tipo: faker.helpers.arrayElement(['INE', 'Domicilio', 'Ingresos']), // CHAR(10)
        url_archivo: `https://qarinoanimal.com/docs/${faker.string.alphanumeric(8)}.pdf`,
        fecha_subida: fechaISO(faker.date.past({ years: 1 })),
        estado_revision: faker.helpers.arrayElement(['Aprobado', 'Pendiente', 'Rechazado']), // CHAR(10)
        comentario: 'Muchas gracias por mandar tu documentación completa.',
      };

      const [insertado] = await insertar('documento', documentoDummie, 'Documento');
      if (insertado) listaDocumentosCreados.push({ idDoc: idDocNuevo, idCliente });
    }
  }

  return listaDocumentosCreados;
}

/**
 * FUNCIÓN CORE: Genera Solicitudes de adopción y toda su cadena cronológica
 * (admin_revisa_solicitud_adopcion, documento_tiene_solicitud_adopcion,
 * periodo_prueba, seguimiento).
 */
async function generarSolicitudes(clientesIds, adminsIds, perrosIds, estadosSolicitud, documentosCreados) {
  console.log('⚙️ Iniciando generación de Solicitudes, Revisiones, Seguimientos y Periodos de Prueba...');

  const motivosCortos = [
    'Quiero darle un hogar lleno de amor.',
    'Busco un compañero fiel para mi familia.',
    'Tengo espacio y cariño para darle.',
    'Quiero adoptar por responsabilidad y amor.',
    'Deseo brindarle una segunda oportunidad.',
  ];

  const TOTAL_SOLICITUDES = 20;

  for (let s = 0; s < TOTAL_SOLICITUDES; s++) {
    const idSolicitudNueva = generarId('sol');

    const clienteAleatorio = faker.helpers.arrayElement(clientesIds);
    const perroAleatorio = faker.helpers.arrayElement(perrosIds);
    const adminAleatorio = faker.helpers.arrayElement(adminsIds);
    const estadoAleatorio = faker.helpers.arrayElement(estadosSolicitud);

    const fechaSolicitudBase = faker.date.recent({ days: 60 });
    const fechaResolucion = new Date(fechaSolicitudBase.getTime() + 10 * 24 * 60 * 60 * 1000);

    const solicitudDummie = {
      id_solicitud: idSolicitudNueva,
      id_usuario: clienteAleatorio,
      id_perro: perroAleatorio,
      id_estado: estadoAleatorio.id_estado,
      fecha_solicitud: fechaISO(fechaSolicitudBase),
      motivo: faker.helpers.arrayElement(motivosCortos), // VARCHAR(50)
      tipo_vivienda: faker.helpers.arrayElement(['Casa', 'Depto']), // CHAR(10)
      otras_mascotas: faker.helpers.arrayElement(['No tengo', '1 gato', '2 perros', 'Un conejo']), // CHAR(20)
      integrantes_hogar: faker.number.int({ min: 1, max: 6 }),
      comentario_admin: 'El perfil del solicitante se ve muy estable.',
      fecha_resolucion: fechaISO(fechaResolucion),
    };

    const [solInsertada] = await insertar('solicitud_adopcion', solicitudDummie, 'Solicitud_adopcion');
    if (!solInsertada) continue;

    console.log(`📩 Solicitud procesada: ${idSolicitudNueva} (Cliente: ${clienteAleatorio} -> Perro: ${perroAleatorio})`);

    // 1. Historial de Auditoría (admin_revisa_solicitud_adopcion)
    await insertar(
      'admin_revisa_solicitud_adopcion',
      { id_usuario: adminAleatorio, id_solicitud: idSolicitudNueva, fecha: solicitudDummie.fecha_resolucion },
      'Admin_Revisa_Solicitud_adopcion'
    );

    // 2. Vinculación de Documentos del cliente a esta solicitud
    const docDelCliente = documentosCreados.find((d) => d.idCliente === clienteAleatorio);
    if (docDelCliente) {
      await insertar(
        'documento_tiene_solicitud_adopcion',
        {
          id_documento: docDelCliente.idDoc,
          id_solicitud: idSolicitudNueva,
          fecha_agregado: solicitudDummie.fecha_solicitud,
        },
        'Documento_Tiene_Solicitud_adopcion'
      );
    }

    // 3. Periodo de Prueba y Seguimiento
    const fechaInicioPrueba = new Date(solicitudDummie.fecha_resolucion);

    await insertar(
      'periodo_prueba',
      {
        id_prueba: generarId('pru'),
        id_solicitud: idSolicitudNueva,
        fecha_inicio: fechaISO(fechaInicioPrueba),
        fecha_fin_prevista: fechaISO(new Date(fechaInicioPrueba.getTime() + 7 * 24 * 60 * 60 * 1000)),
        fecha_fin_real: fechaISO(new Date(fechaInicioPrueba.getTime() + 5 * 24 * 60 * 60 * 1000)),
        estatus_prueba: faker.helpers.arrayElement(['Aceptado', 'En curso', 'Rechazado']), // CHAR(20)
        observaciones_seguimiento: 'Falta que lo visiten en su casa para verificar espacio.',
      },
      'Periodo_Prueba'
    );

    await insertar(
      'seguimiento',
      {
        id_seguimiento: generarId('seg'),
        id_solicitud: idSolicitudNueva,
        fecha_inicio: fechaISO(fechaInicioPrueba),
        fecha_fin: fechaISO(new Date(fechaInicioPrueba.getTime() + 1 * 24 * 60 * 60 * 1000)),
        descripcion: 'Hay que dar seguimiento una vez a la semana de forma obligatoria.',
      },
      'Seguimiento'
    );
  }
}

// ============================================================================
// FASE 6: PROGRAMAS Y VOLUNTARIADO (TABLAS NUEVAS)
// ============================================================================

/** Puebla 'programa'. Cada programa pertenece a un admin y a un tipo. */
async function generarProgramas(tiposPrograma, adminsIds) {
  console.log('⚙️ Iniciando generación de Programas de voluntariado...');

  const listaProgramas = [];
  const TOTAL_PROGRAMAS = 12;

  for (let i = 0; i < TOTAL_PROGRAMAS; i++) {
    const idProgramaNuevo = generarId('prg');
    const fechaInicio = faker.date.soon({ days: 60 });
    const fechaFin = new Date(fechaInicio.getTime() + faker.number.int({ min: 1, max: 5 }) * 24 * 60 * 60 * 1000);

    const programaDummie = {
      id_programa: idProgramaNuevo,
      tipo: faker.helpers.arrayElement(tiposPrograma), // FK -> tipo_programa(nombre)
      id_admin: faker.helpers.arrayElement(adminsIds),
      nombre: faker.helpers.arrayElement([
        'Jornada de Esterilización',
        'Feria de Adopción Centro',
        'Paseo Solidario',
        'Campaña de Vacunación',
        'Tarde de Voluntariado',
        'Bazar de Donativos',
      ]),
      estado_del_programa: faker.helpers.arrayElement(['Disponible', 'Cerrado', 'Finalizado']), // CHAR(20)
      descripcion: 'Actividad abierta a voluntarios registrados en la plataforma.',
      fecha_inicio: fechaISO(fechaInicio),
      fecha_fin: fechaISO(fechaFin),
      ubicacion: faker.location.streetAddress(),
      cupo_maximo: faker.number.int({ min: 10, max: 50 }),
    };

    const [insertado] = await insertar('programa', programaDummie, 'Programa');
    if (insertado) listaProgramas.push(programaDummie);
  }

  return listaProgramas;
}

/** Puebla 'se_inscribe' (pivote M:N Usuario <-> Programa), evitando duplicados de PK. */
async function generarInscripciones(todosUsuariosIds, programas) {
  console.log('⚙️ Iniciando generación de Inscripciones a programas...');

  const paresUsados = new Set();

  for (const programa of programas) {
    const cantidadInscritos = faker.number.int({ min: 2, max: 4 });
    const usuariosElegidos = faker.helpers.arrayElements(todosUsuariosIds, cantidadInscritos);

    for (const idUsuario of usuariosElegidos) {
      const llave = `${idUsuario}|${programa.id_programa}`;
      if (paresUsados.has(llave)) continue; // Respeta la PK compuesta (id_usuario, id_programa)
      paresUsados.add(llave);

      await insertar(
        'se_inscribe',
        {
          id_usuario: idUsuario,
          id_programa: programa.id_programa,
          fecha_inscripcion: fechaISO(faker.date.recent({ days: 20 })),
          estado: faker.helpers.arrayElement(['Aceptado', 'Pendiente', 'Cancelado']), // CHAR(20)
          asistencia: faker.datatype.boolean(),
        },
        'Se_inscribe'
      );
    }
  }
}

// ============================================================================
// FASE 7: DONATIVOS
// ============================================================================

/** Puebla 'donativo'. Ahora incluye id_admin, descripcion, estado y cfdi_solicitado. */
async function generarDonativos(clientesIds, adminsIds, documentosCreados) {
  console.log('⚙️ Iniciando generación de Donativos...');

  for (const idCliente of clientesIds) {
    const cantidadDonativos = faker.datatype.boolean({ probability: 0.8 })
      ? faker.number.int({ min: 1, max: 2 })
      : 0;

    for (let d = 0; d < cantidadDonativos; d++) {
      const docAsociado = documentosCreados.find((doc) => doc.idCliente === idCliente);

      const donativoDummie = {
        id_donativo: generarId('don'),
        id_usuario: idCliente,
        id_admin: faker.datatype.boolean() ? faker.helpers.arrayElement(adminsIds) : null,
        id_documento: docAsociado ? docAsociado.idDoc : null,
        folio_fiscal: faker.string.uuid().toUpperCase(), // CHAR(40)
        tipo: faker.helpers.arrayElement(['Monetario', 'Especie']), // CHAR(20)
        monto: faker.number.float({ min: 100, max: 5000, fractionDigits: 2 }),
        descripcion: 'Donativo registrado durante campaña de apoyo al refugio.',
        fecha: fechaISO(faker.date.recent({ days: 90 })),
        comprobante: null, // BYTEA, se deja nulo en dummies para optimizar
        estado: faker.helpers.arrayElement(['Pendiente de validación', 'Validado', 'Rechazado']),
        cfdi_solicitado: faker.datatype.boolean(),
        comentario_admin: faker.datatype.boolean() ? 'Comprobante verificado correctamente.' : null,
      };

      await insertar('donativo', donativoDummie, 'Donativo');
    }
  }
}

// ============================================================================
// ORQUESTADOR PRINCIPAL
// ============================================================================
async function ejecutarSeeding() {
  console.log('🚀 INICIANDO AUTOMATIZACIÓN DE DUMMIES (Línea de Ensamblaje) 🚀\n');

  await preguntarLimpieza();

  // ---- FASE 1: Catálogos ----
  const nombresRoles = await poblarRoles();
  const nombresPrivilegios = await poblarPrivilegios();
  await poblarRolTienePrivilegio(nombresRoles, nombresPrivilegios);
  const razasGuardadas = await poblarRazas();
  const tamanosGuardados = await poblarTamanos();
  const caracteresGuardados = await poblarCaracteres();
  const cuidadosGuardados = await poblarCuidados();
  const estadosSolicitudGuardados = await poblarEstadosSolicitud();
  const tiposProgramaGuardados = await poblarTiposPrograma();
  const cpsGuardados = await poblarCPs();
  const mensajesGuardados = await poblarMensajes();
  console.log('\n🛑 FASE 1 COMPLETADA: Catálogos maestros listos.');

  // ---- FASE 2: Usuarios, subtipos, datos confidenciales y direcciones ----
  const { clientes: clientesIds, admins: adminsIds, todos: todosUsuariosIds } =
    await generarUsuarios(cpsGuardados);
  console.log('\n🛑 FASE 2 COMPLETADA: Usuarios, direcciones y datos confidenciales listos.');

  // ---- FASE 3: Notificaciones ----
  await generarNotificaciones(clientesIds, mensajesGuardados);
  console.log('\n🛑 FASE 3 COMPLETADA: Notificaciones tejidas.');

  // ---- FASE 4: Módulo Canino ----
  const perrosIds = await generarPerrosYEcosistema(
    razasGuardadas,
    tamanosGuardados,
    caracteresGuardados,
    cuidadosGuardados
  );
  console.log(`\n🛑 FASE 4 COMPLETADA: ${perrosIds.length} perros, galerías y pivotes médicos insertados.`);

  // ---- FASE 5: Documentos y Solicitudes ----
  const documentosCreados = await generarDocumentos(clientesIds);
  await generarSolicitudes(clientesIds, adminsIds, perrosIds, estadosSolicitudGuardados, documentosCreados);
  console.log('\n🛑 FASE 5 COMPLETADA: Documentos, solicitudes y seguimientos insertados.');

  // ---- FASE 6: Programas y Voluntariado ----
  const programasCreados = await generarProgramas(tiposProgramaGuardados, adminsIds);
  await generarInscripciones(todosUsuariosIds, programasCreados);
  console.log('\n🛑 FASE 6 COMPLETADA: Programas e inscripciones insertados.');

  // ---- FASE 7: Donativos ----
  await generarDonativos(clientesIds, adminsIds, documentosCreados);
  console.log('\n🛑 FASE 7 COMPLETADA: Donativos insertados.');

  console.log('\n✅ SEEDING TERMINADO. Todas las tablas de ScriptBD.sql fueron pobladas.');
}

ejecutarSeeding().catch((err) => console.error('💥 Falla catastrófica en el script:', err));

// ==== REGLAS DE PREFIJOS DE IDs (solo para lectura de logs, no se guardan en BD) ====
/*
• Seguridad:
	• (rol usa Nombre como PK, sin prefijo)
	• (privilegio usa Privilegio como PK, sin prefijo)
• Módulo Perfiles / Cuentas:
	• usr_ (Usuario)
	• dir_ (Direcciones_Fisicas)
	• cp_  (Catalogo_de_CPs)
	• (datos_confidenciales comparte PK con Cliente/Usuario)
• Módulo Canino:
	• dog_ (Perro)
	• raz_ (Raza)
	• tam_ (Tamano)
	• car_ (Caracter)
	• cui_ (Cuidado_Especial)
	• mul_ (Multimedia)
• Módulo Trámites y Operaciones:
	• sol_ (Solicitud_adopcion)
	• pru_ (Periodo_Prueba)
	• seg_ (Seguimiento)
	• doc_ (Documento)
	• don_ (Donativo)
• Módulo Comunicación y Voluntariado:
	• msg_ (Mensaje)
	• prg_ (Programa)
	• (tipo_programa usa Nombre como PK, sin prefijo)
	• est_ (Estado_solicitud)
*/
