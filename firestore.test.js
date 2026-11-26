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
const ADMIN_UID = "admin_789";
const ADMIN_EMAIL = "imranabdulshakoor00@gmail.com";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

function validUserPayload(uid, overrides = {}) {
  const now = new Date(Date.now() - 2000);
  return {
    userId: uid,
    username: "PlayerOne",
    fullName: "Alice Gamer",
    email: "alice@example.com",
    avatarKey: "royal",
    coinBalance: 1000,
    dailyRewardPoints: 0,
    totalGamesPlayed: 0,
    totalRewardsEarned: 0,
    loginStreak: 0,
    lastDailyClaimDay: "",
    lastWheelSpinDay: "",
    dailyWheelSpinsUsed: 0,
    accountStatus: "ACTIVE",
    role: "PLAYER",
    createdAt: now,
    updatedAt: now,
    ...overrides,
  };
}

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

test("Unauthenticated users cannot read or write any collection", async () => {
  const unauth = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauth.collection("users").doc(ALICE_UID).get());
  await assertFails(unauth.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID)));
  await assertFails(unauth.collection("leaderboard").get());
  await assertFails(unauth.collection("announcements").get());
  await assertFails(unauth.collection("appConfig").doc("global").get());
  await assertFails(unauth.collection("activityLogs").get());
});

test("Owner can create valid profile, update allowed fields, and cannot escalate role or spoof id", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID, {
    email: "alice@example.com",
    email_verified: true,
  }).firestore();

  // Reject self-assigned ADMIN role
  await assertFails(
    alice.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID, { role: "ADMIN" }))
  );

  // Reject extra @DocumentId field
  await assertFails(
    alice.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID, { id: ALICE_UID }))
  );

  // Valid create succeeds
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).set(validUserPayload(ALICE_UID))
  );

  // Partial update of allowed fields succeeds
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).update({
      coinBalance: 1250,
      totalGamesPlayed: 1,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Reject mutating role or accountStatus or createdAt
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      role: "ADMIN",
      updatedAt: new Date(Date.now() - 200),
    })
  );
  await assertFails(
    alice.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 100),
      updatedAt: new Date(Date.now() - 100),
    })
  );
});

test("Cross-user isolation blocks reading or modifying another user's profile and subcollections", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("users").doc(BOB_UID).set(validUserPayload(BOB_UID));
    await db.collection("users").doc(BOB_UID).collection("transactions").doc("tx1").set({
      userId: BOB_UID,
      type: "WELCOME_BONUS",
      title: "Welcome Bonus",
      amount: 1000,
      balanceAfter: 1000,
      createdAt: new Date(Date.now() - 1000),
    });
  });

  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(alice.collection("users").doc(BOB_UID).get());
  await assertFails(alice.collection("users").get());
  await assertFails(alice.collection("users").doc(BOB_UID).collection("transactions").get());
  await assertFails(alice.collection("users").doc(BOB_UID).update({ coinBalance: 999999 }));
});

test("Owner subcollections (transactions, gameHistory, notifications) enforce schema and ownership", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("transactions").doc("tx_1").set({
      userId: ALICE_UID,
      type: "GAME_WIN",
      title: "Lucky Wheel Win",
      amount: 250,
      balanceAfter: 1250,
      gameId: "lucky_wheel",
      createdAt: now,
    })
  );
  await assertSucceeds(alice.collection("users").doc(ALICE_UID).collection("transactions").get());

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("gameHistory").doc("gh_1").set({
      userId: ALICE_UID,
      gameId: "lucky_wheel",
      gameName: "Lucky Wheel",
      outcome: "WIN",
      entryCoins: 0,
      rewardCoins: 250,
      summary: "Landed on 250 Coins",
      createdAt: now,
    })
  );

  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("notifications").doc("n_1").set({
      userId: ALICE_UID,
      category: "DAILY_REWARD",
      title: "Daily Reward Ready",
      body: "Claim your daily virtual bonus!",
      readStatus: "UNREAD",
      createdAt: now,
    })
  );
  await assertSucceeds(
    alice.collection("users").doc(ALICE_UID).collection("notifications").doc("n_1").update({
      readStatus: "READ",
    })
  );
});

test("Leaderboard allows public non-PII reads and restricts writes to own PLAYER entry or Admin", async () => {
  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  const now = new Date(Date.now() - 1000);

  await assertSucceeds(
    alice.collection("leaderboard").doc(ALICE_UID).set({
      userId: ALICE_UID,
      username: "PlayerOne",
      avatarKey: "royal",
      weeklyPoints: 500,
      monthlyPoints: 500,
      allTimePoints: 1500,
      totalGamesPlayed: 4,
      entryType: "PLAYER",
      updatedAt: now,
    })
  );

  // Reject creating SAMPLE_DEMO as regular player
  await assertFails(
    alice.collection("leaderboard").doc("sample_1").set({
      userId: "sample_1",
      username: "GoldKnight",
      avatarKey: "crown",
      weeklyPoints: 5000,
      monthlyPoints: 12000,
      allTimePoints: 25000,
      totalGamesPlayed: 40,
      entryType: "SAMPLE_DEMO",
      updatedAt: now,
    })
  );

  await assertSucceeds(alice.collection("leaderboard").get());
});

test("Activity logs enforce owner-filtered queries for players and allow full access for verified admins", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    const db = ctx.firestore();
    await db.collection("activityLogs").doc("log_1").set({
      userId: ALICE_UID,
      username: "PlayerOne",
      action: "SLOT_SPIN",
      detail: "Won 150 virtual coins",
      pointsDelta: 150,
      createdAt: new Date(Date.now() - 1000),
    });
  });

  const alice = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(alice.collection("activityLogs").where("userId", "==", ALICE_UID).get());
  await assertFails(alice.collection("activityLogs").get());

  // Spoofed unverified admin email must be rejected
  const spoofedAdmin = testEnv.authenticatedContext("spoof_uid", {
    email: ADMIN_EMAIL,
    email_verified: false,
  }).firestore();
  await assertFails(spoofedAdmin.collection("activityLogs").get());
  await assertFails(
    spoofedAdmin.collection("announcements").doc("ann_1").set({
      title: "Fake",
      message: "Spoof",
      badge: "ALERT",
      authorEmail: ADMIN_EMAIL,
      createdAt: new Date(Date.now() - 1000),
    })
  );

  // Verified admin succeeds
  const realAdmin = testEnv.authenticatedContext(ADMIN_UID, {
    email: ADMIN_EMAIL,
    email_verified: true,
  }).firestore();
  await assertSucceeds(realAdmin.collection("activityLogs").get());
  await assertSucceeds(
    realAdmin.collection("announcements").doc("ann_1").set({
      title: "Weekend Arcade Boost",
      message: "Double daily virtual streak rewards active!",
      badge: "EVENT",
      authorEmail: ADMIN_EMAIL,
      createdAt: new Date(Date.now() - 1000),
    })
  );
  await assertSucceeds(
    realAdmin.collection("appConfig").doc("global").set({
      dailyBaseBonus: 300,
      dailyStreakIncrement: 75,
      maxDailyWheelSpins: 5,
      disabledGames: [],
      maintenanceBanner: "",
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});
