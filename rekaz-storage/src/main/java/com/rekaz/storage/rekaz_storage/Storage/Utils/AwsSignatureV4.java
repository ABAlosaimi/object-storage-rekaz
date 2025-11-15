package com.rekaz.storage.rekaz_storage.Storage.Utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;

public class AwsSignatureV4 {

    private static final String ALGORITHM = "AWS4-HMAC-SHA256";
    private static final String SERVICE = "s3";
    private static final String REQUEST_TYPE = "aws4_request";

    public static Map<String, String> generateHeaders(
            String method,
            String uri,
            byte[] payload,
            String host,
            String region,
            String accessKey,
            String secretKey,
            String contentType) {

        try {
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC); // for consistency and compatibility with AWS
            String amzDate = now.format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'"));
            String dateStamp = now.format(DateTimeFormatter.ofPattern("yyyyMMdd"));

            // Hash payload
            String payloadHash = sha256Hex(payload);

            // Canonical headers 
            String canonicalHeaders = "host:" + host + "\n" +
                                      "x-amz-content-sha256:" + payloadHash + "\n" +
                                      "x-amz-date:" + amzDate + "\n";

            String signedHeaders = "host;x-amz-content-sha256;x-amz-date";

            // Canonical request
            String canonicalRequest = method + "\n" +
                                      uri + "\n" +
                                            "\n" + // Empty query string because we are not using any query parameters in our case 
                                      canonicalHeaders + "\n" +
                                      signedHeaders + "\n" +
                                      payloadHash;

            // String to sign
            String credentialScope = dateStamp + "/" + region + "/" + SERVICE + "/" + REQUEST_TYPE;

            String stringToSign = ALGORITHM + "\n" +
                                  amzDate + "\n" +
                                  credentialScope + "\n" +
                                  sha256Hex(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            // Signing key
            byte[] signingKey = getSignatureKey(secretKey, dateStamp, region, SERVICE);

            String signature = bytesToHex(hmacSHA256(stringToSign, signingKey)); // the crepyptographic signing step

            // inform AWS that we are using Signature V4 in the Authorization header 
            String authorizationHeader = ALGORITHM + " " +
                                        "Credential=" + accessKey + "/" + credentialScope + ", " +
                                        "SignedHeaders=" + signedHeaders + ", " +
                                        "Signature=" + signature;

            
            Map<String, String> headers = new TreeMap<>();
            headers.put("Authorization", authorizationHeader);
            headers.put("x-amz-date", amzDate);
            headers.put("x-amz-content-sha256", payloadHash);
            headers.put("Host", host);
            if (contentType != null && !contentType.isEmpty()) {
                headers.put("Content-Type", contentType);
            }
            headers.put("Content-Length", String.valueOf(payload.length));

            return headers;

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate AWS Signature V4", e);
        }
    }

    private static byte[] getSignatureKey(String key, String dateStamp, String region, String service) throws Exception {
        byte[] kSecret = ("AWS4" + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(region, kDate);
        byte[] kService = hmacSHA256(service, kRegion);
        return hmacSHA256(REQUEST_TYPE, kService);
    }

    private static byte[] hmacSHA256(String data, byte[] key) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private static String sha256Hex(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(data);
        return bytesToHex(hash);
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}
