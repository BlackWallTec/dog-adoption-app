// ============================================================================
// REFUGIO DE MASCOTAS - SCRIPT DE POBLADO DE DATOS (SEEDING Complementario)
// ============================================================================
// Este script automatiza la inserción de dummies para las tablas restantes de la base de datos.
// Ejecución en terminal: node seedComplimenter.js
// Este script fue generado completamente por IA (Gemini) y adaptado a las necesidades del proyecto.
// ============================================================================

import { createClient } from '@supabase/supabase-js';
import { fakerES_MX as faker } from '@faker-js/faker';
import 'dotenv/config';

const supabase = createClient(process.env.SUPABASE_URL, process.env.SUPABASE_SERVICE_ROLE_KEY);

async function poblarTablasFaltantes() {
  console.log("🚀 Iniciando seeding complementario...\n");

  console.log("📥 Obteniendo registros existentes...");
  const { data: admins } = await supabase.from('admin').select('id_usuario');
  const { data: clientes } = await supabase.from('cliente').select('id_usuario');
  const { data: perros } = await supabase.from('perro').select('id_perro');
  const { data: roles } = await supabase.from('rol').select('nombre');
  const { data: privilegios } = await supabase.from('privilegio').select('privilegio');
  const { data: mensajes } = await supabase.from('mensaje').select('id_mensaje');
  const { data: tiposPrograma } = await supabase.from('tipo_programa').select('nombre');
  const { data: estadosSolicitud } = await supabase.from('estado_solicitud').select('id_estado');

  if (!clientes?.length || !admins?.length || !perros?.length) {
    console.error("❌ Error: Faltan Clientes, Admins o Perros base.");
    return;
  }

  // 1. Catálogos y Relaciones
  const cps = [
    { id_codigo_postal: faker.string.uuid(), codigo_postal: '76000', colonia: 'Centro', municipio: 'Querétaro', estado: 'Querétaro' },
    { id_codigo_postal: faker.string.uuid(), codigo_postal: '76100', colonia: 'Juriquilla', municipio: 'Querétaro', estado: 'Querétaro' }
  ];
  await supabase.from('catalogo_de_cps').upsert(cps);

  if (roles?.length && privilegios?.length) {
    const rolesPrivilegios = [];
    for (const rol of roles) {
      const numPrivs = rol.nombre === 'Administrador' ? 3 : 1;
      const privsAsignados = faker.helpers.arrayElements(privilegios, numPrivs);
      for (const p of privsAsignados) {
        rolesPrivilegios.push({ nombre_rol: rol.nombre, privilegio: p.privilegio });
      }
    }
    await supabase.from('rol_tiene_privilegio').upsert(rolesPrivilegios, { onConflict: 'privilegio, nombre_rol', ignoreDuplicates: true });
  }

  const programasIds = [];
  if (tiposPrograma?.length) {
    for (let i = 0; i < 3; i++) {
      const idProg = faker.string.uuid();
      programasIds.push(idProg);
      await supabase.from('programa').insert({
        id_programa: idProg,
        tipo: faker.helpers.arrayElement(tiposPrograma).nombre,
        id_usuario: faker.helpers.arrayElement(admins).id_usuario, // Referencia al Admin
        nombre: `Programa ${faker.word.adjective()}`,
        descripcion: faker.lorem.sentence(),
        fecha_inicio: faker.date.soon({ days: 10 }).toISOString(),
        fecha_fin: faker.date.soon({ days: 20 }).toISOString(),
        ubicacion: faker.location.streetAddress(),
        cupo_maximo: faker.number.int({ min: 10, max: 50 }),
        activo: true
      });
    }
  }

  // 2. Datos Dependientes del Cliente
  const documentosGlobales = [];

  for (const cliente of clientes) {
    const idUsuario = cliente.id_usuario;

    await supabase.from('direcciones_fisicas').insert({
      id_direccion: faker.string.uuid(),
      id_usuario: idUsuario,
      id_codigo_postal: faker.helpers.arrayElement(cps).id_codigo_postal,
      calle: faker.location.street(),
      numero_exterior: faker.number.int({ min: 1, max: 999 }), // Entero
      numero_interior: null
    });

    await supabase.from('datos_confidenciales').insert({
      id_usuario: idUsuario,
      rfc: faker.string.alphanumeric({ length: 13, casing: 'upper' }),
      razon_social: faker.company.name(),
      regimen_fiscal: '601',
      codigo_postal: faker.helpers.arrayElement(['76000', '76100']),
      correo_facturacion: faker.internet.email(),
      constancia_url: `https://sat.gob.mx/${faker.string.uuid()}.pdf`
    });

    if (mensajes?.length) {
      await supabase.from('notifica').insert({
        id_usuario: idUsuario,
        id_mensaje: faker.helpers.arrayElement(mensajes).id_mensaje,
        fecha_envio: faker.date.recent({ days: 5 }).toISOString(),
        leido: faker.datatype.boolean()
      });
    }

    if (programasIds.length > 0 && faker.datatype.boolean()) {
      await supabase.from('se_inscribe').insert({
        id_usuario: idUsuario,
        id_programa: faker.helpers.arrayElement(programasIds),
        fecha_inscripcion: faker.date.recent({ days: 2 }).toISOString().split('T')[0],
        estado: faker.helpers.arrayElement(['Confirmado', 'Pendiente']),
        asistio: faker.datatype.boolean()
      });
    }

    const idDoc = faker.string.uuid();
    documentosGlobales.push({ id_documento: idDoc, id_usuario: idUsuario });
    await supabase.from('documento').insert({
      id_documento: idDoc,
      id_usuario: idUsuario,
      tipo: 'INE',
      url_archivo: `https://storage.com/${idDoc}.pdf`,
      fecha_subida: faker.date.recent({ days: 30 }).toISOString().split('T')[0],
      estado_revision: 'Aprobado',
      comentario: 'OK'
    });

    if (faker.datatype.boolean()) {
      await supabase.from('donativo').insert({
        id_donativo: faker.string.uuid(),
        id_usuario: idUsuario,
        id_documento: idDoc,
        tipo: 'Económico',
        estado: 'Validado',
        monto: faker.number.int({ min: 100, max: 2000 }),
        descripcion: 'Donativo mensual',
        fecha: faker.date.recent({ days: 10 }).toISOString().split('T')[0],
        cfdi_solicitado: faker.datatype.boolean(),
        comentario_admin: 'Gracias'
      });
    }
  }

  // 3. Flujo de Adopción
  for (let i = 0; i < 10; i++) {
    const idSol = faker.string.uuid();
    const clienteAsignado = faker.helpers.arrayElement(clientes).id_usuario;
    const adminAsignado = faker.helpers.arrayElement(admins).id_usuario;
    
    await supabase.from('solicitud_adopcion').insert({
      id_solicitud: idSol,
      id_usuario: clienteAsignado, // FK a usuario
      id_perro: faker.helpers.arrayElement(perros).id_perro,
      id_estado: estadosSolicitud ? faker.helpers.arrayElement(estadosSolicitud).id_estado : null,
      fecha_solicitud: faker.date.past({ years: 1 }).toISOString().split('T')[0],
      motivo: faker.lorem.sentence(),
      tipo_vivienda: faker.helpers.arrayElement(['Casa', 'Departamento']),
      otras_mascotas: faker.datatype.boolean(), // Booleano
      integrantes_hogar: faker.number.int({ min: 1, max: 5 }),
      comentario_admin: 'En orden.',
      fecha_resolucion: faker.date.recent({ days: 5 }).toISOString().split('T')[0]
    });

    await supabase.from('admin_revisa').insert({
      id_usuario: adminAsignado, // FK a admin
      id_solicitud: idSol,
      fecha: new Date().toISOString().split('T')[0]
    });

    const docCliente = documentosGlobales.find(d => d.id_usuario === clienteAsignado);
    if (docCliente) {
      await supabase.from('solicitud_tiene_documento').insert({
        id_documento: docCliente.id_documento,
        id_solicitud: idSol
      });
    }

    await supabase.from('periodo_prueba').insert({
      id_prueba: faker.string.uuid(),
      id_solicitud: idSol,
      fecha_inicio: faker.date.recent({ days: 20 }).toISOString().split('T')[0],
      fecha_fin_prevista: faker.date.soon({ days: 10 }).toISOString().split('T')[0],
      estatus_prueba: 'En curso',
      observaciones_seguimiento: 'Bien'
    });

    await supabase.from('seguimiento').insert({
      id_seguimiento: faker.string.uuid(),
      id_solicitud: idSol,
      fecha_inicio: faker.date.recent({ days: 15 }).toISOString().split('T')[0],
      fecha_fin: faker.date.soon({ days: 15 }).toISOString().split('T')[0],
      descripcion: 'Llamada exitosa.'
    });
  }

  console.log("\n✅ Poblado complementario completado.");
}

poblarTablasFaltantes().catch(console.error);