const functions = require("firebase-functions");
const admin = require("firebase-admin");
// const log = require("firebase-functions/logger");

exports.sendNotification = functions.https.onCall(async (data, context) => {
  // log(data["data"]);
  const payload = data["data"];
  const destinationUid = payload["destinationUid"];

  if (!destinationUid || !payload) {
    throw new functions.https.HttpsError(
        "invalid-argument",
        "destinationUid and payload are required",
    );
  }

  // Fetch User's FCM tokens from Firestore
  const userDoc = await admin.firestore()
      .collection("users")
      .doc(destinationUid)
      .get();

  if (!userDoc.exists) {
    // log(`User ${destinationUid} not found`);
    return {success: false, message: "User not found"};
  }

  const tokens = userDoc.get("deviceTokensFCM") || [];

  if (tokens.length === 0) {
    // log(`No device tokens for user ${destinationUid}`);
    return {success: false, message: "User has no tokens"};
  }

  // Send notification to all tokens
  const message = {
    data: payload,
    tokens: tokens,
  };

  const response = await admin.messaging().sendEachForMulticast(message);

  return {success: true, response, message: "Sent"};
});
