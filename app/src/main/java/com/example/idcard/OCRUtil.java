package com.example.idcard;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;
import com.google.gson.JsonObject;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class OCRUtil {


    private static final String SECRET_ID = "";
    private static final String SECRET_KEY = "";
    private static final String ENDPOINT = "ocr.tencentcloudapi.com";

    // 使用Context和Uri，支持正反面识别
    public static String recognizeIdCard(Context context, Uri imageUri, String cardSide) throws Exception {
        // 1. 从Uri读取图片并转为Base64
        String imageBase64 = imageToBase64(context, imageUri);

        // 2. 构建请求参数
        JsonObject params = new JsonObject();
        params.addProperty("ImageBase64", imageBase64);
        params.addProperty("CardSide", cardSide); // FRONT 或 BACK

        // 3. 构造签名和请求头
        String payload = params.toString();
        TreeMap<String, String> headers = buildHeaders(payload);

        // 4. 发送请求
        return sendRequest(payload, headers);
    }

    // 兼容旧版本的方法
    public static String recognizeIdCard(Context context, Uri imageUri) throws Exception {
        return recognizeIdCard(context, imageUri, "FRONT");
    }

    private static String imageToBase64(Context context, Uri uri) throws Exception {
        byte[] buffer = FileUtil.getFileBytes(context, uri);
        return Base64.encodeToString(buffer, Base64.NO_WRAP);
    }

    private static TreeMap<String, String> buildHeaders(String payload) throws Exception {
        TreeMap<String, String> headers = new TreeMap<>();

        // 1. 获取当前时间戳
        long timestamp = System.currentTimeMillis() / 1000;
        String timestampStr = String.valueOf(timestamp);
        
        // 2. 获取日期（UTC格式）
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String date = sdf.format(new Date(timestamp * 1000));

        // 3. 拼接规范请求串
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json; charset=utf-8\n" + "host:" + ENDPOINT + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(payload);
        String canonicalRequest = httpRequestMethod + "\n" + canonicalUri + "\n" + canonicalQueryString + "\n" 
                + canonicalHeaders + "\n" + signedHeaders + "\n" + hashedRequestPayload;

        // 4. 拼接待签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String service = "ocr";
        String credentialScope = date + "/" + service + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);
        String stringToSign = algorithm + "\n" + timestampStr + "\n" + credentialScope + "\n" + hashedCanonicalRequest;

        // 5. 计算签名
        byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, service);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        byte[] signatureBytes = hmacSha256(secretSigning, stringToSign);
        String signature = bytesToHex(signatureBytes);

        // 6. 拼接 Authorization
        String authorization = algorithm + " " + "Credential=" + SECRET_ID + "/" + credentialScope + ", " 
                + "SignedHeaders=" + signedHeaders + ", " + "Signature=" + signature;

        // 7. 设置请求头
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/json; charset=utf-8");
        headers.put("Host", ENDPOINT);
        headers.put("X-TC-Action", "IDCardOCR");
        headers.put("X-TC-Timestamp", timestampStr);
        headers.put("X-TC-Version", "2018-11-19");
        headers.put("X-TC-Region", "ap-guangzhou");

        return headers;
    }

    private static String sendRequest(String payload, TreeMap<String, String> headers) throws Exception {
        URL url = new URL("https://" + ENDPOINT);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);

            for (String key : headers.keySet()) {
                conn.setRequestProperty(key, headers.get(key));
            }

            conn.getOutputStream().write(payload.getBytes(StandardCharsets.UTF_8));

            int responseCode = conn.getResponseCode();
            
            // 读取响应
            java.io.InputStream inputStream;
            if (responseCode == 200) {
                inputStream = conn.getInputStream();
            } else {
                inputStream = conn.getErrorStream();
            }
            
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(inputStream, StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            
            if (responseCode == 200) {
                return response.toString();
            } else {
                throw new Exception("HTTP错误 " + responseCode + ": " + response.toString());
            }
        } finally {
            conn.disconnect();
        }
    }

    private static String sha256Hex(String data) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] hash = md.digest(data.getBytes(StandardCharsets.UTF_8));
        return bytesToHex(hash);
    }

    private static byte[] hmacSha256(byte[] key, String data) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}