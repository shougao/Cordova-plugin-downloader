# cordova-plugin-downloader2
Cordova downloader plugin for Android. The plugin reads url and refresh timer, download file and store in device storage. It can custom update download progress.

## install
```
cordova plugin add cordova-plugin-downloader2
```

## usage
``Downloader`` is a ``cordova.plugin.downloader.Downloader`` download progress will show on success(message)
```
function success(message){
    console.log("progress = " + message);
}

function error(message){
    console.log("error: reason is " + message);
}

url = "http://yourcompany.com/abc.apk";
Downloader.download(url, success, error);
```

the out put is json object, please using key ``UPDATE_PROGRESS``,``FILE_PATH`` and  ``ERROR`` to get the result.
```
{"UPDATE_PROGRESS":3}
{"UPDATE_PROGRESS":23}
{"UPDATE_PROGRESS":58}
{"UPDATE_PROGRESS":100}
```
the default refresh download progress is 500ms, you could set the timer using ``setRefreshTime``
```
Downloader.setRefreshTime(1000);
```
this plugin need get the two android premission
```
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
```