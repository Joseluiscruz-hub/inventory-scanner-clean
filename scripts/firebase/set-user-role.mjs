import admin from "firebase-admin";

const [, , email, requestedRole, requestedPlanta] = process.argv;
const allowedRoles = new Set(["ADMIN", "OPERADOR"]);

if (!email || !requestedRole) {
  console.error(
    "Uso: npm run set-role -- usuario@dominio.com ADMIN|OPERADOR [plantaId]"
  );
  console.error("Ejemplo OPERADOR: npm run set-role -- op@example.com OPERADOR Norte");
  console.error("Ejemplo ADMIN:     npm run set-role -- admin@example.com ADMIN");
  process.exit(1);
}

const role = requestedRole.trim().toUpperCase();
if (!allowedRoles.has(role)) {
  console.error(`Rol invalido: ${requestedRole}. Usa ADMIN u OPERADOR.`);
  process.exit(1);
}

const planta =
  requestedPlanta && requestedPlanta.trim().length > 0
    ? requestedPlanta.trim()
    : null;

if (role === "OPERADOR" && !planta) {
  console.error(
    "OPERADOR requiere claim planta. Pasa el id de planta como tercer argumento."
  );
  process.exit(1);
}

const serviceAccountPath = process.env.GOOGLE_APPLICATION_CREDENTIALS;
if (!serviceAccountPath) {
  console.error(
    "Falta GOOGLE_APPLICATION_CREDENTIALS apuntando al JSON de service account."
  );
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.applicationDefault()
});

try {
  const user = await admin.auth().getUserByEmail(email);
  const currentClaims = user.customClaims ?? {};
  const nextClaims = {
    ...currentClaims,
    role
  };
  if (planta) {
    nextClaims.planta = planta;
  }

  await admin.auth().setCustomUserClaims(user.uid, nextClaims);

  console.log(
    `Claims actualizados para ${email} (${user.uid}): role=${role}` +
      (planta ? `, planta=${planta}` : " (sin cambio forzado de planta)")
  );
  console.log(
    "El usuario debe cerrar sesion y volver a entrar para refrescar el token."
  );
} catch (error) {
  console.error(`No se pudo asignar claims: ${error.message}`);
  process.exit(1);
}
