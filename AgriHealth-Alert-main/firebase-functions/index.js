const {setGlobalOptions} = require("firebase-functions");
const admin = require("firebase-admin");
setGlobalOptions({maxInstances: 10});
admin.initializeApp();

exports.sendNotification = require("./send-notification.js");
exports.verifyEmail = require("./verify-email.js");
