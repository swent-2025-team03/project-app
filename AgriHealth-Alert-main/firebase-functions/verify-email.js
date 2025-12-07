const functions = require("firebase-functions");
const admin = require("firebase-admin");

admin.initializeApp();

exports.verifyEmail = functions.https.onCall(async (context) => {


  if (!context.auth) {
    throw new functions.https.HttpsError(
        "unauthenticated",
        "user must be logged in",
    );
  }

  const response = await admin.auth().updateUser(context.auth.uid, {
        emailVerified: true
      });

  return
}