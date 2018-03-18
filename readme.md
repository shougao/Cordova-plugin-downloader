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

the out put is below, when progress is 100 you will get the file path in storage.
```
progress = 14
progress = 39
progress = 58
progress = 79
progress = 100
/storage/emulated/0/Download/abc.apk
```
the default refresh download progress is 500ms, you could set the timer using ``setRefreshTime``
```
Downloader.setRefreshTime(1000);
```