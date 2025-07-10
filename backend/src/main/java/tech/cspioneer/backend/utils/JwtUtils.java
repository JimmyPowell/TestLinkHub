package tech.cspioneer.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT encoder/decoder using HMAC-SHA256, adapted for Spring Boot.
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            String payloadJson = objectMapper.writeValueAsString(payload);
            String headerBase = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payloadBase = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signature = sign(headerBase + "." + payloadBase);
            return headerBase + "." + payloadBase + "." + signature;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Unable to serialize payload to JSON", e);
        }
    }

    /**
     * Decode a JWT string into a payload map.
     */
    public Map<String, Object> decode(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }
            String payloadJson = new String(base64UrlDecode(parts[1]), StandardCharsets.UTF_8);
            return objectMapper.readValue(payloadJson, new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            throw new RuntimeException("Unable to deserialize payload from JSON", e);
        }
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

    private String base64UrlEncode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private byte[] base64UrlDecode(String str) {
        return Base64.getUrlDecoder().decode(str);
    }
}
