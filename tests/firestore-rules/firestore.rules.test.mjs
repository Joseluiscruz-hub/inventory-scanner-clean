/**
 * Minimal Firestore rules unit tests (M2 best-effort).
 *
 * Run (with emulator):
 *   cd tests/firestore-rules && npm install
 *   npx firebase emulators:exec --only firestore --project demo-isp "npm test"
 *
 * Without emulator these tests skip gracefully so CI can still load the file.
 *
 * Coverage intent:
 *   - deny cross-plant read for OPERADOR
 *   - deny client inventarios stock write
 */
import { describe, it, before, after } from "node:test";
import assert from "node:assert/strict";
import { readFileSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const RULES_PATH = resolve(__dirname, "../../firestore.rules");

let harness = null;

async function tryLoadHarness() {
  if (!existsSync(RULES_PATH)) {
    return null;
  }
  try {
    const testing = await import("@firebase/rules-unit-testing");
    const rules = readFileSync(RULES_PATH, "utf8");
    const env = await testing.initializeTestEnvironment({
      projectId: "demo-inventoryscannerpro",
      firestore: { rules, host: "127.0.0.1", port: 8080 }
    });
    return { testing, env };
  } catch (err) {
    console.warn("[rules-tests] emulator/harness unavailable, skipping:", err.message);
    return null;
  }
}

before(async () => {
  harness = await tryLoadHarness();
});

after(async () => {
  if (harness?.env) {
    await harness.env.cleanup();
  }
});

describe("firestore.rules plant isolation + stock authority", () => {
  it("rules file exists and mentions inventarios deny writes", () => {
    assert.ok(existsSync(RULES_PATH), "firestore.rules missing");
    const rules = readFileSync(RULES_PATH, "utf8");
    assert.match(rules, /match \/inventarios\/\{documentId\}/);
    assert.match(rules, /allow create, update, delete: if false/);
    assert.match(rules, /claimPlanta|planta/);
  });

  it("OPERADOR cannot read other plant inventarios", async (t) => {
    if (!harness) {
      t.skip("Firebase rules emulator not running");
      return;
    }
    const { testing, env } = harness;
    await env.withSecurityRulesDisabled(async (context) => {
      const adminDb = context.firestore();
      await adminDb.doc("inventarios/Norte__SKU1__ALM1__N_A").set({
        sku: "SKU1",
        almacen: "ALM1",
        tipoTarima: "N/A",
        pallets: 1,
        restos: 0,
        planta: "Norte"
      });
      await adminDb.doc("inventarios/Other__SKU1__ALM1__N_A").set({
        sku: "SKU1",
        almacen: "ALM1",
        tipoTarima: "N/A",
        pallets: 1,
        restos: 0,
        planta: "OtherPlant"
      });
    });

    const op = env.authenticatedContext("op1", {
      role: "OPERADOR",
      planta: "Norte"
    }).firestore();

    await testing.assertSucceeds(
      op.doc("inventarios/Norte__SKU1__ALM1__N_A").get()
    );
    await testing.assertFails(
      op.doc("inventarios/Other__SKU1__ALM1__N_A").get()
    );
  });

  it("client cannot write inventarios stock", async (t) => {
    if (!harness) {
      t.skip("Firebase rules emulator not running");
      return;
    }
    const { testing, env } = harness;
    const op = env.authenticatedContext("op2", {
      role: "OPERADOR",
      planta: "Norte"
    }).firestore();
    await testing.assertFails(
      op.doc("inventarios/Norte__SKU2__ALM1__N_A").set({
        sku: "SKU2",
        almacen: "ALM1",
        tipoTarima: "N/A",
        pallets: 99,
        restos: 0,
        planta: "Norte"
      })
    );
  });
});
