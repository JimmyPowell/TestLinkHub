package tech.cspioneer.backend.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Minimal JWT encoder/decoder using HMAC-SHA256.
 */
public final class JwtUtils {
    private static final String HMAC_ALG = "HmacSHA256";
    private static final AtomicReference<String> PRIVATE_KEY = new AtomicReference<>();
    private static final String CONFIG_PATH = "src/main/resources/application.yml";

    private JwtUtils() {
    }

    /**
     * Generate a JWT access token.
     */
    public static String generateAccessToken(String userUuid, String identity, long expireMillis) {
        Map<String, Object> payload = new HashMap<>();
        long now = System.currentTimeMillis();
        payload.put("uid", userUuid);
        payload.put("identity", identity);
        payload.put("exp", now + expireMillis);
        payload.put("iat", now);
        return encode(payload);
    }

    /**
     * Generate a JWT refresh token with a version number.
     */
    public static String generateRefreshToken(String userUuid, String identity, long expireMillis, int version) {
        Map<String, Object> payload = new HashMap<>();
        long now = System.currentTimeMillis();
        payload.put("uid", userUuid);
        payload.put("identity", identity);
        payload.put("exp", now + expireMillis);
        payload.put("iat", now);
        payload.put("ver", version);
        return encode(payload);
    }

    /**
     * Refresh a refresh token by updating its expiration time.
     */
    public static String refreshToken(String refreshToken, long newExpireMillis) {
        Map<String, Object> payload = decode(refreshToken);
        long now = System.currentTimeMillis();
        payload.put("exp", now + newExpireMillis);
        payload.put("iat", now);
        return encode(payload);
    }

    /**
     * Encode payload to a JWT string.
     */
    public static String encode(Map<String, Object> payload) {
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
    public static Map<String, Object> decode(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
        return parseJson(payloadJson);
    }

    private static String sign(String data) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALG);
            mac.init(new SecretKeySpec(getPrivateKey().getBytes(StandardCharsets.UTF_8), HMAC_ALG));
            byte[] raw = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64UrlEncode(raw);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("unable to sign", e);
        }
    }

    private static String getPrivateKey() {
        if (PRIVATE_KEY.get() == null) {
            synchronized (PRIVATE_KEY) {
                if (PRIVATE_KEY.get() == null) {
                    PRIVATE_KEY.set(loadPrivateKey());
                }
            }
        }
        return PRIVATE_KEY.get();
    }

    private static String loadPrivateKey() {
        try (BufferedReader reader = new BufferedReader(new FileReader(CONFIG_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("privateKey")) {
                    String[] arr = line.split(":");
                    if (arr.length > 1) {
                        return arr[1].trim();
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read config: " + CONFIG_PATH, e);
        }
        throw new IllegalStateException("privateKey not found in config");
    }

    private static String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }

    // Very small JSON writer suitable for simple maps with primitive values.
    private static String toJson(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for (Map.Entry<String, Object> e : map.entrySet()) {
            if (!first) {
                sb.append(',');
            }
            first = false;
            sb.append('"').append(e.getKey()).append('"').append(':');
            Object v = e.getValue();
            if (v instanceof Number) {
                sb.append(v);
            } else {
                sb.append('"').append(Objects.toString(v)).append('"');
            }
        }
        sb.append('}');
        return sb.toString();
    }

    // Very small JSON parser supporting flat objects with string/number values.
    private static Map<String, Object> parseJson(String json) {
        Map<String, Object> map = new HashMap<>();
        String content = json.trim();
        if (content.startsWith("{") && content.endsWith("}")) {
            content = content.substring(1, content.length() - 1).trim();
        }
        if (content.isEmpty()) {
            return map;
        }
        String[] parts = content.split(",");
        for (String part : parts) {
            String[] kv = part.split(":", 2);
            if (kv.length == 2) {
                String key = trimQuotes(kv[0].trim());
                String value = kv[1].trim();
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    map.put(key, trimQuotes(value));
                } else {
                    try {
                        map.put(key, Long.parseLong(value));
                    } catch (NumberFormatException e) {
                        map.put(key, value);
                    }
                }
            }
        }
        return map;
    }

    private static String trimQuotes(String str) {
        if (str.startsWith("\"") && str.endsWith("\"")) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
}