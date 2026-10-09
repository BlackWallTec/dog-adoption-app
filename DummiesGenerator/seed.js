// ============================================================================
// REFUGIO DE MASCOTAS - SCRIPT DE POBLADO DE DATOS (SEEDING)
// ============================================================================
// Este script automatiza la inserción de dummies con integridad referencial total.
// Ejecución en terminal: node seed.js
// Este script fue generado completamente por IA (Gemini) y adaptado a las necesidades del proyecto.
// ============================================================================

import { createClient } from '@supabase/supabase-js';
import { fakerES_MX as faker } from '@faker-js/faker';
import 'dotenv/config'; 

// ==========================================================================
// Sección de Borrado Inicial
// =========================================================================

import readline from 'node:readline/promises';
import { stdin as input, stdout as output } from 'node:process';
import { randomUUID } from 'node:crypto';


// Pregunta si se deben limpiar los datos existentes en Supabase.
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

    const { error } = await supabase.rpc('limpiar_datos_prueba');

    if (error) {
      throw new Error(`Error durante la limpieza: ${error.message}`);
    }

    console.log('✅ Limpieza completada correctamente.');
  } finally {
    terminal.close();
  }
}

// Inicialización del cliente de Supabase usando Service Role (Bypass de políticas RLS)
const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);

/**
 * UTILERÍA: Generador de Identificadores Personalizados (IDs con Prefijos)
 * Genera una cadena única compatible con VARCHAR(36) usando un prefijo elegido.
 * Ejemplo: generarIdConPrefijo('usr') -> 'usr_a1b2c3d4e5f6g7h8'
 * 
 * @param {string} prefijo - El prefijo corto de la tabla (ej: 'usr', 'dog', 'sol')
 * @returns {string} ID formateado de un máximo de 36 caracteres.
 */
function generarIdConPrefijo(prefijo) { return crypto.randomUUID(); }

// ============================================================================
// FASE 1: FUNCIONES PARA POBLAR CATÁLOGOS MAESTROS (TABLAS INDEPENDIENTES)
// ============================================================================

/**
 * Puebla la tabla 'Rol'
 * Nota: Almacenamos el Nombre exacto ya que actúa como la Primary Key (PK).
 */
async function poblarCatálogoRoles() {
  console.log("🔹 Insertando catálogo de Roles...");
  
  const roles = [
    { nombre: 'Administrador', descripcion: 'Control total del refugio y aprobaciones', activo: true },
    { nombre: 'Cliente', descripcion: 'Usuario interesado en adoptar o donar', activo: true }
  ];

  const { error } = await supabase.from('rol').insert(roles);
  if (error) console.error("❌ Error en Catálogo Roles:", error.message);
  
  // Devolvemos los nombres que usamos para tenerlos en memoria RAM mas adelante
  return ['Administrador', 'Cliente'];
}

/**
 * Puebla la tabla 'Privilegio'
 */
async function poblarCatálogoPrivilegios() {
  console.log("🔹 Insertando catálogo de Privilegios...");

  const privilegios = [
    { privilegio: 'Puede subir fotos' },
    { privilegio: 'Puede aprobar solicitudes' },
    { privilegio: 'Puede registrar donativos' },
    { privilegio: 'Inscripción a programas' }
  ];

  const { error } = await supabase.from('privilegio').insert(privilegios);
  if (error) console.error("❌ Error en Catálogo Privilegios:", error.message);

  return privilegios.map(p => p.privilegio);
}

/**
 * Puebla la tabla 'Raza'
 * Genera IDs únicos con prefijo 'raz_' y guarda los objetos completos para usarlos en el módulo canino.
 */
async function poblarCatálogoRazas() {
  console.log("🔹 Insertando catálogo de Razas...");

  const razasBase = [
    { nombre: 'Mestizo', descripcion: 'Cruza única e invaluable' },
    { nombre: 'Pastor Alemán', descripcion: 'Perro guardián, inteligente y leal' },
    { nombre: 'Golden Retriever', descripcion: 'Amigable, juguetón y excelente con niños' },
    { nombre: 'Chihuahua', descripcion: 'Pequeño de tamano pero con gran personalidad' }
  ];

  // Transformamos el array base para inyectarle nuestros IDs con prefijos personalizados
  const registros = razasBase.map(raza => ({
    id_raza: generarIdConPrefijo('raz'),
    nombre: raza.nombre,
    descripcion: raza.descripcion // Sincronizado con tu Diccionario de Datos que usa acento
  }));

  const { error } = await supabase.from('raza').insert(registros);
  if (error) console.error("❌ Error en Catálogo Razas:", error.message);

  // Retornamos el arreglo completo. Guardar el ID de estas filas en memoria evitará romper llaves foráneas.
  return registros;
}

/**
 * Puebla la tabla 'tamano'
 */
async function poblarCatálogotamanos() {
  console.log("🔹 Insertando catálogo de tamanos...");

  const tamanosBase = [
    { nombre: 'Chico', descripcion: 'Menor a 45cm de altura' },
    { nombre: 'Mediano', descripcion: 'De 45 a 75cm de altura' },
    { nombre: 'Grande', descripcion: 'Mayor a 75cm de altura' }
  ];

  const registros = tamanosBase.map(tam => ({
    id_tamano: generarIdConPrefijo('tam'),
    nombre: tam.nombre,
    descripcion: tam.descripcion
  }));

  const { error } = await supabase.from('tamano').insert(registros);
  if (error) console.error("❌ Error en Catálogo tamanos:", error.message);

  return registros;
}

/**
 * Puebla la tabla 'Caracter'
 */
async function poblarCatálogoCaracteres() {
  console.log("🔹 Insertando catálogo de Caracteres...");

  const caracteresBase = [
    { nombre: 'Juguetón', descripcion: 'Mucha energía, ideal para familias activas' },
    { nombre: 'Tranquilo', descripcion: 'Poco ruidoso, ideal para departamentos' },
    { nombre: 'Amigable con niños', descripcion: 'Súper paciente y dócil con infantes' },
    { nombre: 'Protector', descripcion: 'Alerta a su entorno y territorial' }
  ];

  const registros = caracteresBase.map(car => ({
    id_caracter: generarIdConPrefijo('car'),
    nombre: car.nombre,
    descripcion: car.descripcion
  }));

  const { error } = await supabase.from('caracter').insert(registros);
  if (error) console.error("❌ Error en Catálogo Caracteres:", error.message);

  return registros;
}

/**
 * Puebla la tabla 'Cuidado_Especial'
 */
async function poblarCatálogoCuidados() {
  console.log("🔹 Insertando catálogo de Cuidados Especiales...");

  const cuidadosBase = [
    'Dieta estricta libre de pollo',
    'Medicamento diario para articulaciones',
    'Limpieza constante de orejas por propensión a otitis',
    'Ninguno (Salud óptima)'
  ];

  const registros = cuidadosBase.map(desc => ({
    id_cuidado: generarIdConPrefijo('cui'),
    descripcion: desc
  }));

  const { error } = await supabase.from('cuidado_especial').insert(registros);
  if (error) console.error("❌ Error en Catálogo Cuidados:", error.message);

  return registros;
}

/**
 * Puebla la tabla 'Estado_solicitud'
 */
async function poblarCatálogoEstadosSolicitud() {
  console.log("🔹 Insertando catálogo de Estados de Solicitud...");

  const estados = ['Pendiente de Revisión', 'En Periodo de Prueba', 'Aprobada Definitiva', 'Rechazada'];

  const registros = estados.map(desc => ({
    id_estado: generarIdConPrefijo('est'),
    descripcion: desc
  }));

  const { error } = await supabase.from('estado_solicitud').insert(registros);
  if (error) console.error("❌ Error en Catálogo Estados:", error.message);

  return registros;
}

/**
 * Puebla la tabla 'Tipo de Programa'
 */
async function poblarCatálogoTiposPrograma() {
  console.log("🔹 Insertando catálogo de Tipos de Programa...");

  const tipos = [
    { nombre: 'Recaudación Monetaria', descripcion: 'Eventos masivos para recolectar fondos de ayuda' },
    { nombre: 'Cuidado de Perros', descripcion: 'El voluntariado ayuda directamente con el baño y paseo' },
    { nombre: 'Pasarela de Adopción', descripcion: 'Eventos públicos para mostrar perritos disponibles' }
  ];

  // De acuerdo a tu Diccionario de Datos, la PK es la columna 'Nombre' (VARCHAR 36)
  const registros = tipos.map(t => ({
    nombre: t.nombre,
    descripcion: t.descripcion
  }));

  const { error } = await supabase.from('tipo_programa').insert(registros);
  if (error) console.error("❌ Error en Catálogo Tipos Programa:", error.message);

  return registros.map(r => r.nombre);
}

// ============================================================================
// Fin FASE 1
// ============================================================================


// ============================================================================
// FASE 2 Y 3: GENERACIÓN DE USUARIOS, ENTIDADES DERIVADAS Y RELACIONES
// ============================================================================

/**
 * UTILERÍA DE APOYO: Genera un Catálogo rápido de Códigos Postales en memoria.
 * Requerido para poder asignarle una dirección física válida a los usuarios.
 */
async function poblarCatálogoCPs() {
  console.log("🔹 Insertando catálogo de Códigos Postales...");
  const registros = [
    { id_codigo_postal: generarIdConPrefijo('cp'), codigo_postal: '76000', colonia: 'Centro Histórico', municipio: 'Querétaro', estado: 'Querétaro', pais: 'México' },
    { id_codigo_postal: generarIdConPrefijo('cp'), codigo_postal: '01000', colonia: 'San Ángel', municipio: 'Álvaro Obregón', estado: 'CDMX', pais: 'México' }
  ];
  await supabase.from('catálogo_de_cps').insert(registros);
  return registros; // Retornamos para tener los IDs de los CPs disponibles
}

/**
 * UTILERÍA DE APOYO: Genera Mensajes base en el sistema.
 * Requerido para demostrar la relación Muchos a Muchos (M:N) de notificaciones.
 */
async function poblarCatálogoMensajes() {
  console.log("🔹 Insertando catálogo de Mensajes del sistema...");
  const registros = [
    { id_mensaje: generarIdConPrefijo('msg'), titulo: 'Cita de Adopción', texto: 'Le recordamos su cita mañana a las 10:00 AM.', tipo: 'Notificación' },
    { id_mensaje: generarIdConPrefijo('msg'), titulo: 'Donativo Recibido', texto: 'Muchas gracias por tu apoyo monetario.', tipo: 'Agradecimiento' }
  ];
  await supabase.from('mensaje').insert(registros);
  return registros;
}


/**
 * FUNCIÓN CORE: Genera Usuarios (Admins y Clientes) y teje sus relaciones.
 * Aquí explicamos a fondo cómo automatizar la integridad referencial.
 * 
 * @param {Array} cpsDisponibles - Viene de la libreta de CPs de la Fase 1
 * @param {Array} mensajesDisponibles - Viene de la libreta de Mensajes de la Fase 1
 */
async function generarUsuariosYRelaciones(cpsDisponibles, mensajesDisponibles) {
  console.log("⚙️ Iniciando generación masiva de Usuarios y Relaciones...");

  // Creamos listas vacías para guardar los IDs de los actores que van a nacer en este ciclo
  const listaClientesCreados = [];
  const listaAdminsCreados = [];

  // Decidimos generar 10 usuarios en total para esta prueba
  for (let i = 0; i < 10; i++) {
    
    // Decisión de negocio lógica: Los primeros 2 serán Administradores, el resto Clientes.
    const esAdmin = i < 2; 
    const rolElegido = esAdmin ? 'Administrador' : 'Cliente';

    // Generamos el ID primario del usuario de una vez en el script
    const idUsuarioNuevo = generarIdConPrefijo('usr');

    // 1. CREAMOS EL REGISTRO PADRE (Tabla: Usuario)
    const usuarioDummie = {
      id_usuario: idUsuarioNuevo, // Le inyectamos nuestro ID con prefijo 'usr_'
      nombre_rol: rolElegido,            // FK obligatoria hacia la tabla Rol (Fase 1)
      nombre: faker.person.firstName(),
      apellidos: faker.person.lastName(),
      correo: faker.internet.email(),
      telefono: faker.phone.number({ style: 'national' }), // String de 10 dígitos MX
      contrasena: '\$2b\$12\$eImiTXuWVxfM37uY4JANje...',       // Hash estático simulado
      fecha_nacimiento: faker.date.birthdate({ min: 18, max: 60, mode: 'age' }).toISOString().split('T')[0], // Solo YYYY-MM-DD
      fecha_registro: faker.date.past({ years: 1 }) // Registrado en algún punto del último año
    };

    const { error: errUser } = await supabase.from('usuario').insert(usuarioDummie);
    if (errUser) {
      console.error("❌ Error al insertar Usuario base:", errUser.message);
      continue; // Si falla el padre, saltamos este ciclo para no generar huérfanos
    }

    // ------------------------------------------------------------------------
    // ¿CÓMO GENERAR RELACIONES DUMMIE? - CASO 1: HERENCIA DIRECTA (1:1 / Subtipos)
    // ------------------------------------------------------------------------
    // Tu modelo indica que 'Cliente' y 'Admin' son extensiones de 'Usuario'.
    // Comparten exactamente la misma llave primaria que funciona a la vez como llave foránea.
    if (esAdmin) {
      await supabase.from('admin').insert({ id_usuario: idUsuarioNuevo });
      listaAdminsCreados.push(idUsuarioNuevo); // Guardamos en memoria para futuras revisiones
      console.log(`👤 Admin registrado: ${usuarioDummie.nombre} (${idUsuarioNuevo})`);
    } else {
      await supabase.from('cliente').insert({ id_usuario: idUsuarioNuevo });
      listaClientesCreados.push(idUsuarioNuevo); // Guardamos en memoria para adopciones/donativos
      console.log(`👥 Cliente registrado: ${usuarioDummie.nombre} (${idUsuarioNuevo})`);

      // ------------------------------------------------------------------------
      // ¿CÓMO GENERAR RELACIONES DUMMIE? - CASO 2: RELACIÓN 1 A MUCHOS (1:N)
      // ------------------------------------------------------------------------
      // Un Cliente puede tener direcciones físicas. La tabla 'Direcciones_fisicas' pide:
      // a) Un ID_Usuario (FK del Cliente que acabamos de crear)
      // b) Un ID_Codigo_postal (FK de un CP real creado previamente)
      
      // Pasos lógicos:
      // Elegimos un CP al azar de nuestra libreta de la Fase 1 usando faker.helpers.arrayElement
      const cpAleatorio = faker.helpers.arrayElement(cpsDisponibles);

      const direccionDummie = {
        id_direccion: generarIdConPrefijo('dir'),
        id_usuario: idUsuarioNuevo,               // Conexión legítima al Cliente actual (Integridad)
        id_codigo_postal: cpAleatorio.id_codigo_postal, // Conexión legítima al CP seleccionado al azar
        calle: faker.location.street(),
        numero_exterior: faker.number.int({ min: 1, max: 500 }),
        numero_interior: faker.helpers.arrayElement([null, faker.number.int({ min: 1, max: 20 })]) // Puede no tener número interior
      };

      await supabase.from('direcciones_fisicas').insert(direccionDummie);


      // ------------------------------------------------------------------------
      // ¿CÓMO GENERAR RELACIONES DUMMIE? - CASO 3: RELACIÓN MUCHOS A MUCHOS (M:N)
      // ------------------------------------------------------------------------
      // Muchos clientes reciben muchos mensajes. Esto se resuelve con la tabla pivote 
      // 'cliente_notifica_mensaje'. Para llenarla, necesitamos cruzar los dos mundos.
      
      // Pasos lógicos:
      // Seleccionamos un mensaje al azar de la lista que nos pasaron por parámetro
      const mensajeAleatorio = faker.helpers.arrayElement(mensajesDisponibles);

      const notificacionPivoteDummie = {
        id_usuario: idUsuarioNuevo,               // El ID del cliente actual
        id_mensaje: mensajeAleatorio.id_mensaje,  // El ID del mensaje estático aleatorio
        Fecha_envio: faker.date.recent({ days: 30 }), // Enviado en los últimos 30 días
        Leido: faker.datatype.boolean()           // TRUE o FALSE aleatorio
      };

      // Al insertar en la pivote, Supabase une los cables sin romper restricciones
      await supabase.from('notifica').insert(notificacionPivoteDummie);
    }
  }

  // Al terminar el bucle, le regresamos al orquestador los IDs de los clientes y admins reales
  return { clientes: listaClientesCreados, admins: listaAdminsCreados };
}

// ============================================================================
// Fin FASE 2 Y 3
// ============================================================================

// ============================================================================
// FASE 4: MÓDULO CANINO (PERROS, CARACTERES, CUIDADOS Y MULTIMEDIA)
// ============================================================================

/**
 * FUNCIÓN CORE: Genera un volumen masivo de Perros y amarra sus características.
 * 
 * @param {Array} razasDisponibles - Catálogo de Razas de la Fase 1
 * @param {Array} tamanosDisponibles - Catálogo de tamanos de la Fase 1
 * @param {Array} caracteresDisponibles - Catálogo de Caracteres de la Fase 1
 * @param {Array} cuidadosDisponibles - Catálogo de Cuidados de la Fase 1
 */
async function generarPerrosYEcosistema(razasDisponibles, tamanosDisponibles, caracteresDisponibles, cuidadosDisponibles) {
  console.log("⚙️ Iniciando generación masiva de Perritos y su ecosistema...");

  // Lista en memoria para guardar los IDs de los perritos que van a nacer
  const listaPerrosCreados = [];

  // URLs reales de perritos en Unsplash para que tu app en Kotlin renderice fotos reales y hermosas
  const fotosDePerritos = [
    "https://unsplash.com",
    "https://unsplash.com",
    "https://unsplash.com",
    "https://unsplash.com",
    "https://unsplash.com"
  ];
    
  // Vamos a meter 15 perritos de prueba al refugio
  for (let i = 0; i < 15; i++) {
    const idPerroNuevo = generarIdConPrefijo('dog');

    // Seleccionamos llaves foráneas legítimas de nuestros catálogos en memoria
    const razaAleatoria = faker.helpers.arrayElement(razasDisponibles);
    const tamanoAleatorio = faker.helpers.arrayElement(tamanosDisponibles);
    const caracterPrincipalAleatorio = faker.helpers.arrayElement(caracteresDisponibles);
    

    // 1. CREAMOS EL REGISTRO PADRE (Tabla: Perro)
    const perroDummie = {
      id_perro: idPerroNuevo,
      id_raza: razaAleatoria.id_raza,       // FK legítima a Raza
      id_tamano: tamanoAleatorio.id_tamano, // FK legítima a tamano
      id_caracter: caracterPrincipalAleatorio.id_caracter, // FK al Caracter principal
      nombre: faker.person.firstName(),     // Faker no tiene "nombres de perro", pero los de persona quedan divertidos (ej. Bruno, Toby)
      sexo: faker.helpers.arrayElement(['M', 'F']),
      tamano_medida: faker.number.float({ min: 20, max: 90, fractionDigits: 1 }), // Medida en cm (FLOAT según DD)
      edad_aprox: faker.number.int({ min: 1, max: 14 }), // INT según DD (ej: 4)
      descripcion: "Un perrito rescatado, muy noble, ideal para llenar tu hogar de amor.",
      esterilizado: faker.datatype.boolean(),
      vacunado: faker.datatype.boolean(),
      estado: faker.helpers.arrayElement(['Disponible', 'En proceso de adopción', 'Adoptado']),
      fecha_ingreso: faker.date.past({ years: 2 }), // Ingresó en los últimos 2 años
    };

    const { error: errDog } = await supabase.from('perro').insert(perroDummie);
    if (errDog) {
      console.error("❌ Error al insertar Perro:", errDog.message);
      continue;
    }

    listaPerrosCreados.push(idPerroNuevo);
    console.log(`🐾 Perrito registrado: ${perroDummie.nombre} (${idPerroNuevo})`);

    // ------------------------------------------------------------------------
    // ¿CÓMO GENERAR RELACIONES DUMMIE? - CASO 4: PIVOTES M:N MASIVOS
    // ------------------------------------------------------------------------
    // Tu modelo tiene la tabla 'Perro Tiene Caracter' para sumarle MÁS ánimos al perro.
    // Para simular que un perro tiene MÚLTIPLES caracteres, hacemos un pequeño bucle interno.
    
    // Elegimos 2 caracteres aleatorios y diferentes del catálogo
    const caracteresAdicionales = faker.helpers.arrayElements(caracteresDisponibles, 2);
    
    for (const car of caracteresAdicionales) {
      await supabase.from('perro_tiene_caracter').insert({
        id_perro: idPerroNuevo,
        id_caracter: car.id_caracter
      });
    }

    // Hacemos exactamente lo mismo para 'Perro Tiene Cuidado_Especial'
    const cuidadoAleatorio = faker.helpers.arrayElement(cuidadosDisponibles);
    await supabase.from('perro_tiene_cuidado_especial').insert({
      id_perro: idPerroNuevo,
      id_cuidado: cuidadoAleatorio.id_cuidado
    });

    // ------------------------------------------------------------------------
    // ¿CÓMO GENERAR RELACIONES DUMMIE? - CASO 5: GALERÍA DE IMÁGENES (1:N)
    // ------------------------------------------------------------------------
    // Un perro tiene muchas fotos en la tabla 'Multimedia'. 
    // Insertamos 2 registros multimedia por cada perro usando su ID activo.
    for (let m = 0; m < 2; m++) {
      const multimediaDummie = {
        id_multimedia: generarIdConPrefijo('mul'),
        id_perro: idPerroNuevo, // Amarrado legítimamente al perro del ciclo
        url: faker.helpers.arrayElement(fotosDePerritos),
        fecha_subida: faker.date.recent({ days: 15 }), // Subida hace poco
        tipo: faker.helpers.arrayElement(['Foto', 'Video']) // Sincronizado con CHAR(10) de tu DD
      };
      await supabase.from('multimedia').insert(multimediaDummie);
    }
  }

  // Devolvemos la lista de perritos reales en memoria RAM para la fase de adopciones
  return listaPerrosCreados;
}

// ===========================================================================
// Fin FASE 4
// ===========================================================================


// ============================================================================
// FASE 5: OPERACIONES, TRANSACCIONES COMPLEJAS Y SEGUIMIENTOS CRONOLÓGICOS
// ============================================================================

/**
 * FUNCIÓN CORE: Genera Solicitudes, Periodos de Prueba, Seguimientos y Donativos.
 * Cruza los IDs de Clientes, Admins y Perros asegurando consistencia en el tiempo.
 * 
 * @param {Array} clientesIds - Lista de IDs con prefijo 'cli_' de la Fase 2
 * @param {Array} adminsIds - Lista de IDs con prefijo 'adm_' de la Fase 2
 * @param {Array} perrosIds - Lista de IDs con prefijo 'dog_' de la Fase 4
 * @param {Array} estadosSolicitud - Catálogo de Estados de la Fase 1
 */
async function generarOperacionesYTrámites(clientesIds, adminsIds, perrosIds, estadosSolicitud) {
  console.log("⚙️ Iniciando generación de Solicitudes, Revisiones, Donativos y Periodos de Prueba...");

  // Lista para almacenar documentos y asociarlos después a las solicitudes
  const listaDocumentosCreados = [];

  // ------------------------------------------------------------------------
  // SUB-PROCESO PREVIO: Documentos del Cliente
  // Cada cliente subirá de 1 a 2 documentos oficiales indispensables para adoptar
  // ------------------------------------------------------------------------
  for (const idCliente of clientesIds) {
    const cantidadDocs = faker.number.int({ min: 1, max: 2 });
    for (let d = 0; d < cantidadDocs; d++) {
      const idDocNuevo = generarIdConPrefijo('doc');
      
      const documentoDummie = {
        id_documento: idDocNuevo,
        id_usuario: idCliente, // Vinculado directamente al cliente (Integridad)
        tipo: faker.helpers.arrayElement(['pdf', 'jpg', 'png']),
        url_archivo: `https://refugio.com{faker.string.alphanumeric(6)}.pdf`,
        fecha_subida: faker.date.past({ years: 1 }), // Subido en el último año
        estado_revision: faker.helpers.arrayElement(['Aprobado', 'Pendiente', 'Rechazado']),
        comentario: 'Muchas gracias por mandar tu documentación completa.'
      };

      await supabase.from('documento').insert(documentoDummie);
      listaDocumentosCreados.push({ idDoc: idDocNuevo, idCliente: idCliente });
    }

    // ------------------------------------------------------------------------
    // SUB-PROCESO: Donativos Masivos (Muestra de 15 a 20 filas en total)
    // ------------------------------------------------------------------------
    if (faker.datatype.boolean()) { // El 50% de los clientes hacen un donativo
      const docAsociado = listaDocumentosCreados.find(d => d.idCliente === idCliente);
      
      const donativoDummie = {
        id_donativo: generarIdConPrefijo('don'),
        id_usuario: idCliente,
        id_documento: docAsociado ? docAsociado.idDoc : null, // FK opcional al documento del cliente
        folio_fiscal: faker.string.uuid().toUpperCase(), // Simula el UUID largo del SAT
        tipo: faker.helpers.arrayElement(['monetario', 'especie']),
        monto: faker.number.float({ min: 100, max: 5000, fractionDigits: 2 }), // DECIMAL funcional
        fecha: '07-10-2026', // Formateado en string según tu Diccionario de Datos
        comprobante: null // Campo BYTEA (binario), se deja nulo en dummies para optimizar
      };
      await supabase.from('donativo').insert(donativoDummie);
    }
  }

  // ------------------------------------------------------------------------
  // PROCESO TRANSACCIONAL: Creación de 20 Solicitudes de Adopción
  // ------------------------------------------------------------------------
  for (let s = 0; s < 20; s++) {
    const idSolicitudNueva = generarIdConPrefijo('sol');
    
    // Ruleta aleatoria para asignar actores de los arrays en memoria
    const clienteAleatorio = faker.helpers.arrayElement(clientesIds);
    const perroAleatorio = faker.helpers.arrayElement(perrosIds);
    const adminAleatorio = faker.helpers.arrayElement(adminsIds);
    const estadoAleatorio = faker.helpers.arrayElement(estadosSolicitud);

    // CONTROL DEL RELOJ: Generamos una fecha base coherente para la solicitud
    const fechaSolicitudBase = faker.date.recent({ days: 60 });

    const solicitudDummie = {
      id_solicitud: idSolicitudNueva,
      id_usuario: clienteAleatorio, // Debe ser un Cliente legítimo
      id_perro: perroAleatorio,     // Debe ser un Perro legítimo
      id_estado: estadoAleatorio.id_estado, // Estado del catálogo
      fecha_solicitud: fechaSolicitudBase.toISOString().split('T')[0], // Mapeo DATE (YYYY-MM-DD)
      motivo: 'Quiero adoptar un perro para que sea mi compañero de vida.',
      tipo_vivienda: faker.helpers.arrayElement(['casa', 'departamento']),
      otras_mascotas: faker.helpers.arrayElement(['no tengo', 'sí, dos chihuahuas', 'un gato']),
      integrantes_hogar: faker.number.int({ min: 1, max: 6 }), // INT según DD
      comentario_admin: 'El perfil del solicitante se ve muy estable.',
      fecha_resolucion: new Date(fechaSolicitudBase.getTime() + 10 * 24 * 60 * 60 * 1000).toISOString().split('T')[0] // Resuelta exactamente 10 días después
    };

    const { error: errSol } = await supabase.from('solicitud_adopcion').insert(solicitudDummie);
    if (errSol) {
      console.error("❌ Error al crear Solicitud:", errSol.message);
      continue;
    }

    console.log(`📩 Solicitud procesada: ${idSolicitudNueva} (Cliente: ${clienteAleatorio} -> Perro: ${perroAleatorio})`);

    // ------------------------------------------------------------------------
    // CONEXIONES CRONOLÓGICAS DE LA SOLICITUD (Tablas Hijas)
    // ------------------------------------------------------------------------
    
    // 1. Historial de Auditoría (Admin Revisa Solicitud)
    await supabase.from('admin_revisa').insert({
      id_usuario: adminAleatorio, // El ID de un Admin real
      id_solicitud: idSolicitudNueva,
      fecha: solicitudDummie.fecha_resolucion // Ocurre el día de la resolución
    });

    // 2. Vinculación de Documentos que se adjuntaron a esta solicitud específica
    const docDelCliente = listaDocumentosCreados.find(d => d.idCliente === clienteAleatorio);
    if (docDelCliente) {
      await supabase.from('documento_tiene_solicitud_adopcion').insert({
        id_documento: docDelCliente.idDoc,
        id_solicitud: idSolicitudNueva,
        Fecha_agregado: solicitudDummie.fecha_solicitud // Se adjuntó el día que se mandó la solicitud
      });
    }

    // 3. Periodo de Prueba y Seguimiento (Regla de negocio lógica: Solo si el estado es aprobado/prueba)
    // Para simplificar tus pruebas, generaremos registros para simular los flujos avanzados
    const fechaInicioPrueba = new Date(solicitudDummie.fecha_resolucion);
    
    // Tabla: Periodo_Prueba
    await supabase.from('periodo_prueba').insert({
      id_prueba: generarIdConPrefijo('pru'),
      id_solicitud: idSolicitudNueva, // Amarrado a la solicitud actual
      fecha_inicio: fechaInicioPrueba.toISOString().split('T')[0],
      fecha_fin_prevista: new Date(fechaInicioPrueba.getTime() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // +7 días
      fecha_fin_real: new Date(fechaInicioPrueba.getTime() + 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],     // +5 días
      estatus_prueba: 'Aceptado',
      observaciones_seguimiento: 'Falta que lo visiten en su casa para verificar espacio.'
    });

    // Tabla: Seguimiento
    await supabase.from('seguimiento').insert({
      id_seguimiento: generarIdConPrefijo('seg'),
      id_solicitud: idSolicitudNueva,
      fecha_inicio: fechaInicioPrueba.toISOString().split('T')[0],
      Fecha_Fin: new Date(fechaInicioPrueba.getTime() + 1 * 24 * 60 * 60 * 1000).toISOString().split('T')[0], // 1 día de duración de la visita
      descripcion: 'Hay que dar seguimiento una vez a la semana de forma obligatoria.'
    });
  }
}
// ===========================================================================
// Fin FASE 5
// ===========================================================================




// ============================================================================
// ORQUESTADOR PRINCIPAL ACTUALIZADO (FASE 1, 1.5, 2, 3 Y 4)
// ============================================================================
async function ejecutarSeeding() {
  console.log("🚀 INICIANDO AUTOMATIZACIÓN DE DUMMIES (Línea de Ensamblaje) 🚀\n");

   await preguntarLimpieza();

  // ---- FASE 1: Catálogos Fijos ----
  await poblarCatálogoRoles();
  await poblarCatálogoPrivilegios();
  const razasGuardadas = await poblarCatálogoRazas();
  const tamanosGuardados = await poblarCatálogotamanos();
  const caracteresGuardados = await poblarCatálogoCaracteres();
  const cuidadosGuardados = await poblarCatálogoCuidados();
  await poblarCatálogoEstadosSolicitud();
  await poblarCatálogoTiposPrograma();

  // ---- FASE 1.5: Soporte ----
  const cpsGuardados = await poblarCatálogoCPs();
  const mensajesGuardados = await poblarCatálogoMensajes();

  console.log("\n🛑 FASE 1 Y 1.5 COMPLETADA: Infraestructura base lista.");

  // ---- FASE 2 Y 3: Usuarios, Clientes, Admins y Notificaciones ----
  const usuariosEcosistema = await generarUsuariosYRelaciones(cpsGuardados, mensajesGuardados);
  console.log("\n🛑 FASE 2 Y 3 COMPLETADA: Usuarios y relaciones iniciales tejidas.");

  // ---- FASE 4: Módulo Canino ----
  // Inyectamos las libretas de la Fase 1 para que el generador ensamble los perros
  const perrosEcosistema = await generarPerrosYEcosistema(
    razasGuardadas, 
    tamanosGuardados, 
    caracteresGuardados, 
    cuidadosGuardados
  );

  console.log("\n🛑 FASE 4 COMPLETADA: Perros, galerías multimedia y pivotes médicos insertados.");
  console.log(`Perros listos en memoria RAM para adopción: ${perrosEcosistema.length}`);
}

ejecutarSeeding().catch(err => console.error("💥 Falla catastrófica en el script:", err));



// ==== REGLAS DE PREFIJOS DE IDs =====
/*
• Módulo Perfiles / Cuentas:
	• usr_ (Tabla: Usuario)
	• cli_ (Tabla: Cliente)
	• adm_ (Tabla: Admin)
	• dir_ (Tabla: Direcciones_físicas)
	• cp_ (Tabla: Catálogo de CPs)
• Módulo Canino:
	• dog_ (Tabla: Perro)
	• raz_ (Tabla: Raza)
	• tam_ (Tabla: tamano)
	• car_ (Tabla: Caracter)
	• cui_ (Tabla: Cuidado_Especial)
	• mul_ (Tabla: Multimedia)
• Módulo Trámites y Operaciones:
	• sol_ (Tabla: Solicitud_adopción)
	• pru_ (Tabla: Periodo_Prueba)
	• seg_ (Tabla: Seguimiento)
	• doc_ (Tabla: Documento)
	• don_ (Tabla: Donativo)
	• cnf_ (Tabla: Datos_confidenciales)
• Módulo Comunicación y Voluntariado:
	• msg_ (Tabla: Mensaje)
	• prg_ (Tabla: Programa)
	• tpr_ (Tabla: Tipo_programa)
	• prv_ (Tabla: Privilegio)
*/
