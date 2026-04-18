package com.example.idcard;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class OCRUtil {

    // 腾讯云配置（请替换为您的实际密钥）
    private static final String SECRET_ID = "your-secret-id";
    private static final String SECRET_KEY = "your-secret-key";
    private static final String ENDPOINT = "ocr.tencentcloudapi.com";
    private static final String SERVICE = "ocr";
    private static final String REGION = "ap-guangzhou";
    private static final String ACTION = "IDCardOCR";
    private static final String VERSION = "2018-11-19";

    public static String recognizeIdCard(String imagePath) throws Exception {
        // 1. 读取图片并转为Base64
        String imageBase64 = FileUtil.imageToBase64(imagePath);

        // 2. 构建请求参数
        JsonObject params = new JsonObject();
        params.addProperty("ImageBase64", imageBase64);
        // 身份证正面或反面：Front/Back
        params.addProperty("CardSide", "Front");

        // 3. 构造V3签名
        String payload = params.toString();
        TreeMap<String, String> headers = buildHeaders(payload);

        // 4. 发送请求
        OkHttpClient client = new OkHttpClient();
        MediaType mediaType = MediaType.parse("application/json");
        RequestBody body = RequestBody.create(payload, mediaType);

        Request.Builder requestBuilder = new Request.Builder()
                .url("https://" + ENDPOINT)
                .post(body);

        for (String key : headers.keySet()) {
            requestBuilder.addHeader(key, headers.get(key));
        }

        Request request = requestBuilder.build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new Exception("请求失败: " + response.code());
            }
            return response.body().string();
        }
    }

    private static TreeMap<String, String> buildHeaders(String payload) throws Exception {
        TreeMap<String, String> headers = new TreeMap<>();

        // 时间戳
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String timestamp = sdf.format(new Date());

        // 构建规范请求
        String httpRequestMethod = "POST";
        String canonicalUri = "/";
        String canonicalQueryString = "";
        String canonicalHeaders = "content-type:application/json\nhost:" + ENDPOINT + "\n";
        String signedHeaders = "content-type;host";
        String hashedRequestPayload = sha256Hex(payload);

        String canonicalRequest = httpRequestMethod + "\n" +
                canonicalUri + "\n" +
                canonicalQueryString + "\n" +
                canonicalHeaders + "\n" +
                signedHeaders + "\n" +
                hashedRequestPayload;

        // 构建待签名字符串
        String algorithm = "TC3-HMAC-SHA256";
        String requestTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String date = timestamp.substring(0, 10);
        String credentialScope = date + "/" + SERVICE + "/tc3_request";
        String hashedCanonicalRequest = sha256Hex(canonicalRequest);

        String stringToSign = algorithm + "\n" +
                timestamp + "\n" +
                credentialScope + "\n" +
                hashedCanonicalRequest;

        // 计算签名
        byte[] secretDate = hmacSha256(("TC3" + SECRET_KEY).getBytes(StandardCharsets.UTF_8), date);
        byte[] secretService = hmacSha256(secretDate, SERVICE);
        byte[] secretSigning = hmacSha256(secretService, "tc3_request");
        String signature = bytesToHex(hmacSha256(secretSigning, stringToSign));

        // 构造Authorization
        String authorization = algorithm + " " +
                "Credential=" + SECRET_ID + "/" + credentialScope + ", " +
                "SignedHeaders=" + signedHeaders + ", " +
                "Signature=" + signature;

        // 设置请求头
        headers.put("Authorization", authorization);
        headers.put("Content-Type", "application/json");
        headers.put("Host", ENDPOINT);
        headers.put("X-TC-Action", ACTION);
        headers.put("X-TC-Timestamp", requestTimestamp);
        headers.put("X-TC-Version", VERSION);
        headers.put("X-TC-Region", REGION);

        return headers;
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

    private static byte[] hmacSha256(String key, String data) throws Exception {
        return hmacSha256(key.getBytes(StandardCharsets.UTF_8), data);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}