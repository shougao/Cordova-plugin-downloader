package cordova.plugin.downloader;

import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;

import com.arialyy.annotations.Download;
import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTask;

import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.URL;
import java.net.URLDecoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.List;


/**
 * This class echoes a string called from JavaScript.
 */
public class Downloader extends CordovaPlugin {

    private static final int UPDATE = 100;
    private static final int ERROR = 101;

    public static final String KEY_UPDATE_PROGRESS = "UPDATE_PROGRESS";
    public static final String KEY_FILE_PATH = "FILE_PATH";
    public static final String KEY_ERROR = "ERROR";

    private static boolean mStarted = false;
    private CallbackContext mCallbackContext;
    private int mRefreshTime = 500;
    private static final int MIN_REFRESH_TIME = 100;


    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        mCallbackContext = callbackContext;
        if (action.equals("download")) {
            if (mStarted) {
                callbackMessage(KEY_ERROR, "has download task in using.");
                return false;
            }
            Aria.init(cordova.getContext());
            Aria.download(this).register();
            String url = args.getString(0);
            mStarted = true;
            this.download(url, callbackContext);
            return true;
        } else if (action.equals("setRefreshTime")) {
            int time = args.getInt(0);
            mRefreshTime = time > MIN_REFRESH_TIME ? time : mRefreshTime;
            return true;
        } else if (action.equals("getIMEI")) {
            String deviceId = getDeviceId();
            mCallbackContext.success(deviceId);
            return true;
        }
        return false;
    }

    private String getDeviceId() {
        String idString = getLanMac() + getWlanMac();
        return getMd5(idString);
    }

    public String getMd5(final String string) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(string.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String getLanMac() {
        String mac = getMac("eth0");
        System.out.println("eth0 mac : " + mac);
        return mac;
    }

    private String getWlanMac() {
        String mac = getMac("wlan0");
        System.out.println("wlan0 mac : " + mac);
        return mac;
    }

    private String getMac(String interfaceName) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }

                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }

                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
        } // for now eat exceptions
        return "";
    }

    private void callbackMessage(String key, String message) {
        if (mCallbackContext == null) {
            return;
        }
        PluginResult dataResult;
        JSONObject jsonObject = new JSONObject();

        if (key.equals(KEY_UPDATE_PROGRESS)) {
            int progress = Integer.valueOf(message);
            try {
                jsonObject.put(KEY_UPDATE_PROGRESS, progress);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataResult = new PluginResult(PluginResult.Status.OK, jsonObject);
            dataResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(dataResult);
            return;
        } else if (key.equals(KEY_FILE_PATH)) {
            try {
                jsonObject.put(KEY_FILE_PATH, message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataResult = new PluginResult(PluginResult.Status.OK, jsonObject);
            dataResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(dataResult);
            return;
        } else if (key.equals(KEY_ERROR)) {
            try {
                jsonObject.put(KEY_ERROR, message);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            dataResult = new PluginResult(PluginResult.Status.ERROR, jsonObject);
            dataResult.setKeepCallback(true);
            mCallbackContext.sendPluginResult(dataResult);
            mCallbackContext = null;
            return;
        }
    }

    private void download(String url, CallbackContext callbackContext) {
        if (url != null) {
            String downloadPath = Environment.getExternalStorageDirectory() + "/Download/" + getFileName(url.toString());

            Aria.download(this)
                    .load(url)     //读取下载地址
                    .setFilePath(downloadPath) //设置文件保存的完整路径
                    .start();   //启动下载
        } else {
            mStarted = true;
            callbackContext.error("Expected one non-empty string argument.");
        }
    }

    //在这里处理任务执行中的状态，如进度进度条的刷新
    @Download.onTaskRunning protected void running(DownloadTask task) {
        int progressValue = task.getPercent();	//任务进度百分比
        callbackMessage(KEY_UPDATE_PROGRESS, String.valueOf(progressValue));
    }

    @Download.onTaskComplete void taskComplete(DownloadTask task) {
        callbackMessage(KEY_UPDATE_PROGRESS, String.valueOf(100));
        mStarted = false;
    }
/*

    public class DownloadTask extends AsyncTask<String, Integer, File> {

        File targetFile;
        String apkName;


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            apkName = cordova.getActivity().getPackageName();
        }

        @Override
        protected void onPostExecute(File file) {
            mStarted = false;
            if (file == null) {
                callbackMessage(KEY_ERROR, "no file in sd card error.");
            } else {
                callbackMessage(KEY_UPDATE_PROGRESS, String.valueOf(100));
                callbackMessage(KEY_FILE_PATH, file.toString());
            }
            mCallbackContext = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            switch (values[0]) {
                case UPDATE:
                    int progressValue = values[1];
                    callbackMessage(KEY_UPDATE_PROGRESS, String.valueOf(progressValue));
                    break;
                case ERROR:
                    callbackMessage(KEY_ERROR, "net work error, response code =" + values[1]);
                    break;
            }
        }

        @Override
        protected File doInBackground(String... strings) {
            long marktime = System.currentTimeMillis();
            HttpURLConnection connection = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.connect();
                if (connection.getResponseCode() != 200) {
                    publishProgress(ERROR, connection.getResponseCode());
                    return null;
                }
                double contentLen = connection.getContentLength();
                double sumLength = 0;

                BufferedInputStream bis = new BufferedInputStream(connection.getInputStream());
                targetFile = new File(Environment.getExternalStorageDirectory() + "/Download/" + getFileName(url.toString()));
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                int len = -1;
                byte[] bytes = new byte[1024];
                while ((len = bis.read(bytes)) != -1) {
                    bos.write(bytes, 0, len);
                    bos.flush();
                    sumLength += len;
                    if ((System.currentTimeMillis() - marktime) > mRefreshTime) {
                        marktime = System.currentTimeMillis();
                        publishProgress(UPDATE, (int)(sumLength * 100 / contentLen));
                    }
                }
                bos.close();
                bis.close();
            } catch (NullPointerException e) {
                callbackMessage(KEY_ERROR, "download logic NPE.");
                e.printStackTrace();
            } catch (MalformedURLException e) {
                callbackMessage(KEY_ERROR, "url format error.");
            } catch (IOException e) {
                callbackMessage(KEY_ERROR, "network error.");
            } finally {
                if (connection != null){
                    connection.disconnect();
                }
                mStarted = false;
            }
            return targetFile;
        }

    }
*/

    private String getFileName(String url) {
        String fileName = url.substring(url.lastIndexOf("/") + 1, url.length());
        if (TextUtils.isEmpty(fileName) && !fileName.contains(".apk")) {
            fileName = cordova.getActivity().getPackageName() + ".apk";
        }
        try {
            fileName = URLDecoder.decode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return fileName;
    }
}
