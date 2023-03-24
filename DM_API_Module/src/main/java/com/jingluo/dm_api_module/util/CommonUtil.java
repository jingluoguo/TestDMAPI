package com.jingluo.dm_api_module.util;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.jingluo.dm_api_module.constant.Constant;
import com.jingluo.dm_api_module.constant.ConstantUrl;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class CommonUtil {

    private static final String TAG = "CommonUtil";

    /**
     * 对字符串 MD5 无盐值加密
     *
     * @param plainText 传入要加密的字符串
     * @return MD5加密后生成32位(小写字母 + 数字)字符串
     */
    public static String MD5Lower(String plainText) {
        try {
            // 获得MD5摘要算法的 MessageDigest 对象
            MessageDigest md = MessageDigest.getInstance("MD5");

            // 使用指定的字节更新摘要
            md.update(plainText.getBytes());

            // digest()最后确定返回md5 hash值，返回值为8位字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值。1 固定值
            return new BigInteger(1, md.digest()).toString(16);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public interface GetSigCallback {
        void onCallback(int error, String sig);
    }

    public interface CommonCallback {
        void onCallback(int code, JSONObject data, String msg);
    }

    /// post请求
    public static void commonRequestWithHeader(String url, String params, Headers headers, CommonCallback callback) {
        Log.i(TAG, "url: " + url + " params: " + params);
        OkHttpClient client = new OkHttpClient();

        client.newCall(new Request.Builder()
                .url(url)
                .headers(headers)
                .post(RequestBody.create(MediaType.parse("application/json"), params))
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.i(TAG, "http failed");
                callback.onCallback(-1, new JSONObject(), e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                ResponseBody respBody = response.body();

                if (respBody == null) {
                    callback.onCallback(-4, new JSONObject(), "respBody is empty");
                    return;
                }

                String respJson = respBody.string();

                Log.i(TAG, "响应：" + respJson);

                try {
                    JSONObject respJsObj = new JSONObject(respJson);
                    JSONObject Header = new JSONObject();
                    if (!respJsObj.isNull("Header")) {
                        Header = respJsObj.getJSONObject("Header");
                    } else if (!respJsObj.isNull("header")) {
                        Header = respJsObj.getJSONObject("header");
                    }

                    int errorCode = -6;
                    if (!Header.isNull("Code")) {
                        errorCode = (Integer) Header.get("Code");
                    } else if (!Header.isNull("code")) {
                        errorCode = (Integer) Header.get("code");
                    }

                    if (errorCode != 200) {
                        String msg = "";
                        if (!Header.isNull("Message")) {
                            msg = (String) Header.get("Message");
                        } else if (!Header.isNull("message")) {
                            msg = (String) Header.get("message");
                        }
                        callback.onCallback(errorCode, new JSONObject(), msg);
                        return;
                    }

                    JSONObject Payload = new JSONObject();
                    if (!respJsObj.isNull("Payload")) {
                        Payload = respJsObj.getJSONObject("Payload");
                    } else if (!respJsObj.isNull("payload")) {
                        Payload = respJsObj.getJSONObject("payload");
                    }
                    callback.onCallback(0, Payload, "");
                } catch (Exception e) {
                    e.printStackTrace();
                    callback.onCallback(-1, new JSONObject(), e.getMessage());
                }
            }
        });
    }

    /// 签名鉴权
    public static String getRealRequestUrl(String baseUrl) {

        String accessToken = Constant.DEFAULT_APP_ACCESS_TOKEN;
        String fullUrl = ConstantUrl.Yun_Main_Url + baseUrl;

//        List<Map.Entry<String, Object>> list = new ArrayList<Map.Entry<String, Object>>(param.entrySet());
//        // 使用list.sort()排序，按key排序
//        list.sort(new Comparator<Map.Entry<String, Object>>() {
//            @Override
//            public int compare(Map.Entry<String, Object> o1, Map.Entry<String, Object> o2) {
//                return o1.getKey().compareTo(o2.getKey());
//            }
//        });
//        String signatureOrigin = "";
//        for (Map.Entry<String, Object> mapping : list){
//            if (signatureOrigin.equals("")) {
//                signatureOrigin = mapping.getKey() + "=" + mapping.getValue().toString();
//            } else {
//                signatureOrigin = String.format("%s&%s=%s", signatureOrigin, mapping.getKey(), mapping.getValue().toString());
//            }
//        }
        String signatureOrigin = "appkey=" + Constant.DEFAULT_APP_KEY + "&timestamp=" + String.valueOf(new Date().getTime() / 1000);
        String encode = sha256_HMAC(signatureOrigin, accessToken);
        try {
            String baseEncode = URLEncoder.encode(encode, "UTF-8");
            signatureOrigin = String.format("%s&%s=%s", signatureOrigin, "signature", baseEncode);
            fullUrl = String.format("%s?%s", fullUrl, signatureOrigin);
            Log.i(TAG, "获取到的完整路径：" + fullUrl);
        } catch (UnsupportedEncodingException e) {
            Log.i(TAG, "base64加密失败"+e.getMessage());
        }

        return fullUrl;
    }

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param b 字节数组
     * @return 字符串
     */
    private static String byteArrayToHexString(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; b!=null && n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0XFF);
            if (stmp.length() == 1)
                hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

    /**
     * sha256_HMAC加密
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    private static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            hash = Base64.getEncoder().encodeToString(sha256_HMAC.doFinal(message.getBytes()));
            System.out.println(hash);
        } catch (Exception e) {
            System.out.println("Error HmacSHA256 ===========" + e.getMessage());
        }
        return hash;
    }
}
