# Inicio

La página `/admin/inicio` ya se ve. Entran Administrador y Root. Una cuenta Cliente no: se borra la cookie y el login explica que no tiene acceso al panel.

En desarrollo, los números no salen del servidor: salen de unos JSON de prueba que imitan lo que el backend devolvería el día que exista. Fuera de desarrollo ese ejemplo no se publica. La página dice «Próximamente» y el menú no lleva conteos.

Nada de esta carpeta cambia la API ni la base de datos. Perros, Solicitudes, Donativos y Voluntariado siguen trabajando como antes.

Si nunca viste esta forma de organizar archivos, no hace falta aprender un framework. Son carpetas con nombres largos para separar tres trabajos: dibujar, decidir y limpiar datos. Abajo está el camino de un dato, y después una guía de qué abrir según lo que quieras revisar.

## Un pendiente, de punta a punta

Alguien abre `/admin/inicio?escenario=ok` en el servidor de desarrollo, con sesión de Administrador o Root. Esto es lo que pasa con un donativo de Mariana:

1. `inicio.view.js` no sabe de datos. Le dice a `inicio.js`: "arranca".
2. `inicio.js` pide primero la sesión. Si `/me` responde 401, o el rol es Cliente, manda al login y no sigue. Si la sesión sirve, pide a la vez los indicadores, la bandeja, las jornadas y el perro. Si uno falla, los otros siguen.
3. Quien contesta hoy es `InicioRepositoryMock.js`. Mira `?escenario=ok` y abre `data/fixtures/ok.js`. Ahí el donativo llega como lo mandaría el servidor, con espacios de sobra:

```js
persona: { nombre: "Mariana ", apellidos: "Ruiz " }, monto: 1500
```

4. `data/mappers/pendiente.mapper.js` lo limpia: `"Mariana Ruiz"`, fecha de verdad, monto numérico. `domain/entities/` solo describe esa forma; no hace cálculos.
5. `domain/reglas/prioridad.js` ordena la lista. Un correo de factura con error va primero. Después, lo más viejo. Si dos tienen la misma antigüedad, gana la adopción, luego el donativo, luego la factura. Los días están en `umbrales.js`: solicitud 3, donativo 2, factura 3. Pasado ese día, el pendiente se marca atrasado.
6. `inicio.presentar.js` lo vuelve texto y enlace. De ese donativo sale "Validar donativo de Mariana Ruiz", "$1,500.00 · hace 4 días" y `/admin/donativos?abrir=...`. Una jornada no abre Inscritos: esa pantalla lee `id`, y el del ejemplo no es un programa real, así que la tarjeta abre el calendario. Las frases viven en `COPY`, en ese mismo archivo.
7. `inicio.view.js` escribe eso en la página con `textContent`. No arma frases ni direcciones.

El filtro de la bandeja no vuelve a pedir datos. `inicio.js` ya tiene la lista y solo esconde lo que no corresponde. Por eso, al pulsar Facturas, los indicadores de arriba no cambian.

## Qué incluye la feature

Cinco bloques en la página, más el nombre del topbar y dos números en el menú:

- Saludo, fecha, Registrar perro y Crear jornada.
- Cuatro indicadores clicables: solicitudes, donativos, facturas y perros.
- Bandeja "Requiere tu atención", con filtros y como máximo 6 filas.
- Próximas jornadas y, si aplica, el aviso de una semana sin cobertura.
- Un solo perro, "Lleva más tiempo esperando". Si no hay candidato, ese bloque no se dibuja.

El nombre del saludo y del topbar sí sale del servidor (`/api/perfiles/me` y `/api/perfiles/profile`). Si `/me` responde 401, Inicio vuelve al login. Una cuenta Cliente también: se borra la cookie y el login explica que no tiene acceso al panel. Si la sesión falla en otra pantalla, el topbar muestra "—". El resto del dashboard, en desarrollo, sale del ejemplo. Fuera de desarrollo no se publica ese ejemplo.

## Dos ejemplos que se prestan a confusión

**Un bloque cae y la página no.** `?escenario=parcial` hace fallar solo las jornadas. La bandeja y los indicadores siguen. Ese bloque muestra "No pudimos cargar las jornadas" y un botón Reintentar, que vuelve a preguntar únicamente por la agenda.

**El 4 del menú no es el 1 del chip.** En el caso `ok`, Donativos del menú lateral dice 4 y el chip "Donativos" de la bandeja dice 1. No es un bug. El menú suma donativos por validar (2) más facturas pendientes (2). El chip cuenta solo las filas de donativo que cupieron en la bandeja. Solicitudes del menú sí coincide con el indicador "Solicitudes por atender" (2).

## Relevante para el PR

Esto es lo que un revisor debería mirar, en este orden. El resto de los archivos existe para que estos funcionen.

1. La página en el navegador, con los escenarios de más abajo. Ahí se ve si el dashboard se entiende.
2. Los archivos de fuera de esta carpeta. Son pocos y sí tocan al resto del admin:
   - `JS/jsPerfiles/login.js` y `JS/jsPerfiles/accesoPanel.js`: Administrador y Root entran a Inicio. Cliente se queda en el login, sin cookie.
   - `vite.config.js`: registra la página nueva.
   - `JS/rutas.js`: `enlaceA` arma direcciones como `/admin/perros?abrir=abc`. El módulo de destino abre la lista e ignora `abrir`, salvo Crear jornada y el calendario, que sí leen su parámetro.
   - `components/admin-topbar.js`: con sesión muestra el nombre real. Sin sesión, o si falla, muestra "—". Un Cliente lo manda al login.
   - `components/admin-sidebar.js`: en desarrollo, con el ejemplo `ok`, Inicio pinta el 2 y el 4. En las otras pantallas, y fuera de desarrollo, esos números no aparecen.
3. `inicio.view.js` si el comentario es de botones, foco o de algo que se ve mal.
4. `inicio.presentar.js` (`COPY`) si el comentario es de una frase, un color de estado o un enlace.
5. `domain/reglas/prioridad.js` si el comentario es "esto no debería ir primero".

## Se puede ignorar

No hace falta leerlos para aprobar el PR. Están para que el día de mañana el backend real entre sin reescribir la página.

- `domain/entities/`. Son comentarios del estilo "un resumen tiene estos campos". No se ejecutan.
- `domain/repositories/InicioRepository.js`. Lista las cuatro preguntas que cualquier fuente de datos debe saber contestar: resumen, pendientes, agenda y perro. Tampoco tiene lógica.
- `data/fixtures/` salvo `ok.js`. Los otros (`vacio`, `parcial`, `error`, `lento`, `atrasado`) son el mismo tipo de respuesta, vacía, rota o vieja.
- `data/mappers/`, salvo que el comentario sea "este campo del API está mal leído".
- `InicioRepositoryMock.js`. Reparte el fixture que toque. No llama al servidor.
- `*.test.js` y `InicioRepository.contract.js`, si están en verde. Cubren carga, vacío, un bloque roto, reintento, filtro, el orden de la bandeja, que un 401 mande al login una sola vez, que un Cliente no publique el ejemplo y que fuera de desarrollo no haya conteos.

## Si quieres cambiar o revisar algo, ve aquí

| Quiero…                                            | Abre                                      | Ejemplo                                                                        |
| -------------------------------------------------- | ----------------------------------------- | ------------------------------------------------------------------------------ |
| Mover un botón o un título de sección              | `inicio.view.js`                          | El encabezado "Requiere tu atención" se crea ahí. El texto sale de `COPY`.     |
| Cambiar una frase                                  | `inicio.presentar.js`, objeto `COPY`      | "Todo al día" o "No pudimos cargar las jornadas…".                             |
| Cambiar a dónde lleva una tarjeta                  | `inicio.presentar.js`                     | Solicitudes va a `/admin/solicitudes?estado=pendientes`.                       |
| Que algo se marque atrasado antes                  | `domain/reglas/umbrales.js`               | Donativos pasa de 2 a 1 día. La vista no tiene esos números.                   |
| Cambiar quién va primero en la bandeja             | `domain/reglas/prioridad.js`              | Hoy el correo con error gana a una solicitud vieja.                            |
| Ver o editar el ejemplo con datos                  | `data/fixtures/ok.js`                     | El folio `QA-A2222222` y el perro Canela viven ahí.                            |
| Que un campo nuevo del API se vea                  | el mapper de ese dato, en `data/mappers/` | Si el donativo trae `monto` con otro nombre, se cambia el mapper, no la vista. |
| El color de una tarjeta, sin tocar otras pantallas | `CSS/cssInicio/adminInicio.css`           | Solo usa variables de `tokens.inicio.css`. No pongas un `#hex` nuevo aquí.     |
| El nombre del topbar                               | `JS/jsPerfiles/usuarioActual.js`          | Si hay sesión usa el perfil. Si no hay nombre, "—".                            |
| Quién puede entrar al panel                        | `JS/jsPerfiles/accesoPanel.js`            | Administrador y Root. Cliente pierde la cookie y vuelve al login.              |
| Probar un caso sin leer código                     | la dirección, sección de abajo            | `?escenario=parcial`.                                                          |

## Cómo verlo

Con el servidor de desarrollo andando, y una sesión de Administrador o Root:

- `/admin/inicio` o `?escenario=ok` — día con pendientes.
- `?escenario=vacio` — ceros y listas vacías. El perro no aparece.
- `?escenario=parcial` — falla solo la agenda.
- `?escenario=error` — fallan los bloques; el saludo se queda.
- `?escenario=lento` — tarda un momento a propósito, para ver el estado de carga.
- `?escenario=atrasado` — todo lleva muchos días esperando.

En producción esos `?escenario=` se ignoran. La página no carga el ejemplo: muestra "Próximamente" y el menú no lleva el 2 ni el 4. En desarrollo, sin parámetro, el ejemplo sigue siendo `ok`.

## Qué no hace todavía

- No llama a la API de perros, solicitudes, donativos ni voluntariado. En desarrollo esos números son de prueba. Fuera de desarrollo no se muestran.
- Los enlaces abren el módulo, pero ese módulo todavía no abre el registro ni el detalle que viene en la dirección.
- La campana y el buscador del topbar siguen sin hacer nada.
- No hay archivo de logo, así que el logo del menú sigue sin verse.
