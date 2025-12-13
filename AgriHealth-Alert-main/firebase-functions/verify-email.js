const functions = require("firebase-functions");
const admin = require("firebase-admin");

module.exports = functions.https.onCall(async (data, context) => {
  const payload = data["data"];
  const uid = payload["uid"];

  if (!uid || !payload) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "destinationUid and payload are required",
    );
  }

  await admin.auth().updateUser(uid, {
    emailVerified: true,
  });

  return;
});
