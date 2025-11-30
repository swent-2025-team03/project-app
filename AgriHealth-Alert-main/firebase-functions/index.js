const {setGlobalOptions} = require("firebase-functions");
setGlobalOptions({maxInstances: 10});

const {sendNotification} = require("./send-notification.js");
exports.sendNotification = sendNotification;
