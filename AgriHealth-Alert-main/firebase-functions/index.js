const {setGlobalOptions} = require("firebase-functions");
setGlobalOptions({maxInstances: 10});

exports.sendNotification = require("./send-notification.js");
exports.verifyEmail = require("./verify-email.js");
