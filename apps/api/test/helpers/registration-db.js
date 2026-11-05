import { PGlite } from "@electric-sql/pglite";
import { readFile } from "node:fs/promises";

export async function createRegistrationDb() {
  const db = new PGlite();
  await db.exec(await readFile(new URL("./perfiles-schema.sql", import.meta.url), "utf8"));
  await db.exec(
    await readFile(new URL("../../sql/perfiles/001_registro_cliente.sql", import.meta.url), "utf8"),
  );
  return db;
}
