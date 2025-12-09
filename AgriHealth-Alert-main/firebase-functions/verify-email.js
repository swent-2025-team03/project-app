const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.verifyEmail = functions.https.onCall(async (data, context) => {
  const payload = data["data"];
  const uid = payload["uid"];


  await admin.auth().updateUser(uid, {
    emailVerified: true,
  });

  return;
});
