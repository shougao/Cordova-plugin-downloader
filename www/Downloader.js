var exec = require('cordova/exec');

exports.setRefreshTime = function (arg0, success, error) {
    console.log('zqc setRefreshTime');
    cordova.exec(success, error, 'Downloader', 'setRefreshTime', [arg0]);
};

exports.download = function (arg0, success, error) {
    cordova.exec(success, error, 'Downloader', 'download', [arg0]);
};