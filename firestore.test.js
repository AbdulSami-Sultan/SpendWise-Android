const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

// --- SECURITY BOUNDS TESTS ---

test("Unauthenticated user: cannot read expenses", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("expenses").get());
});

test("Unauthenticated user: cannot write expenses", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp1").set({
      userId: ALICE_UID,
      title: "Coffee",
      amount: 4.5,
      category: "Food & Dining",
      date: "2026-11-02",
      createdAt: new Date(),
    })
  );
});

test("Cross-user access: Alice cannot read Bob's expenses", async () => {
  await testEnv.withSecurityRulesDisabled(async (context) => {
    await context.firestore().collection("users").doc(BOB_UID).collection("expenses").doc("bob_exp").set({
      userId: BOB_UID,
      title: "Bob Expense",
      amount: 25.0,
      category: "Shopping",
      date: "2026-11-02",
      createdAt: new Date(),
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("expenses").doc("bob_exp").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("expenses").get());
});

test("Cross-user write: Alice cannot write into Bob's expenses", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("expenses").doc("alice_hack").set({
      userId: BOB_UID,
      title: "Hacked Expense",
      amount: 100.0,
      category: "Shopping",
      date: "2026-11-02",
      createdAt: new Date(),
    })
  );
});

test("Authenticated owner: can create and read own expense", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_1");

  await assertSucceeds(
    docRef.set({
      userId: ALICE_UID,
      title: "Lunch Burrito",
      amount: 12.5,
      category: "Food & Dining",
      date: "2026-11-02",
      notes: "With drink",
      paymentMethod: "Credit Card",
      createdAt: new Date(),
    })
  );

  const snap = await assertSucceeds(docRef.get());
});

test("Validation: rejects invalid expense (negative amount, bad date format)", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const docRef = aliceDb.collection("users").doc(ALICE_UID).collection("expenses").doc("exp_invalid");

  // Negative amount
  await assertFails(
    docRef.set({
      userId: ALICE_UID,
      title: "Invalid",
      amount: -10,
      category: "Food",
      date: "2026-11-02",
      createdAt: new Date(),
    })
  );

  // Bad date
  await assertFails(
    docRef.set({
      userId: ALICE_UID,
      title: "Invalid Date",
      amount: 15.0,
      category: "Food",
      date: "11/02/2026",
      createdAt: new Date(),
    })
  );
});

test("Budgets: owner can create and list budgets", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const budgetRef = aliceDb.collection("users").doc(ALICE_UID).collection("budgets").doc("food_budget");

  await assertSucceeds(
    budgetRef.set({
      userId: ALICE_UID,
      category: "Food & Dining",
      monthlyLimit: 500.0,
      createdAt: new Date(),
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("budgets").get());
});
