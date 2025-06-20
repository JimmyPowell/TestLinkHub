package tech.cspioneer.backend.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Minimal JWT encoder/decoder using HMAC-SHA256, adapted for Spring Boot.
 */
@Component
public final class JwtUtils {
    private static final String HMAC_ALG = "HmacSHA256";

    @Value("${jwt.secret}")
    private String privateKey;

    @Value("${jwt.access-token-expiration-ms}")
    private long accessTokenExpirationMs;

    @Value("${jwt.refresh-token-expiration-ms}")
    private long refreshTokenExpirationMs;

    /**
     * Generate a JWT access token using configured expiration time.
     */
    public String generateAccessToken(String userUuid, String identity) {
        Map<String, Object> payload = new HashMap<>();
        long now = System.currentTimeMillis();
        payload.put("uid", userUuid);
        payload.put("identity", identity);
        payload.put("exp", now + accessTokenExpirationMs);
        payload.put("iat", now);
        return encode(payload);
    }

    /**
     * Generate a JWT refresh token with a version number, using configured expiration time.
     */
    public String generateRefreshToken(String userUuid, String identity, int version) {
        Map<String, Object> payload = new HashMap<>();
        long now = System.currentTimeMillis();
        payload.put("uid", userUuid);
        payload.put("identity", identity);
        payload.put("exp", now + refreshTokenExpirationMs);
        payload.put("iat", now);
        payload.put("ver", version);
        return encode(payload);
    }

    /**
     * Refresh a refresh token by updating its expiration time.
     */
    public String refreshToken(String refreshToken) {
        Map<String, Object> payload = decode(refreshToken);
        long now = System.currentTimeMillis();
        payload.put("exp", now + refreshTokenExpirationMs);
        payload.put("iat", now);
        return encode(payload);
    }

    /**
     * Encode payload to a JWT string.
     */
    public String encode(Map<String, Object> payload) {
        String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
        String payloadJson = toJson(payload);
        String headerBase = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
        String payloadBase = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
        String signature = sign(headerBase + "." + payloadBase);
        return headerBase + "." + payloadBase + "." + signature;
    }

    /**
     * Decode a JWT string into a payload map.
     */
    public Map<String, Object> decode(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        return parseJson(payloadJson);
    }

    private String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(privateKey.getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("unable to sign", e);
        }
    }

    private String toJson(Map<String, Object> payload) {
        StringBuilder json = new StringBuilder("{");
        for (Map.Entry<String, Object> entry : payload.entrySet()) {
            json.append("\"").append(entry.getKey()).append("\":");
            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\",");
            } else {
                json.append(value).append(",");
            }
        }
        if (json.length() > 1) {
            json.setLength(json.length() - 1);
        }
        json.append("}");
        return json.toString();
    }

    private Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new HashMap<>();
        String[] pairs = json.substring(1, json.length() - 1).split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].replace("\"", "").trim();
            String valueStr = keyValue[1].trim();
            if (valueStr.startsWith("\"")) {
                map.put(key, valueStr.substring(1, valueStr.length() - 1));
            } else {
                map.put(key, Long.parseLong(valueStr));
            }
        }
        return map;
    }

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }
}