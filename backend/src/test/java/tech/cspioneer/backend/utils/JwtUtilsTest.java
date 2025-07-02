package tech.cspioneer.backend.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.util.HashMap;
import java.util.Map;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsTest {
    private JwtUtils jwtUtils;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        // 反射注入配置属性
        setField(jwtUtils, "privateKey", "testSecretKey");
        setField(jwtUtils, "accessTokenExpirationMs", 10000L);
        setField(jwtUtils, "refreshTokenExpirationMs", 20000L);
    }

    private void setField(Object obj, String field, Object value) {
        try {
            java.lang.reflect.Field f = obj.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testEncodeAndDecode_Normal() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", "user1");
        payload.put("identity", "admin");
        String token = jwtUtils.encode(payload);
        assertNotNull(token);
        Map<String, Object> decoded = jwtUtils.decode(token);
        assertEquals("user1", decoded.get("uid"));
        assertEquals("admin", decoded.get("identity"));
    }

    @Test
    void testEncode_JsonProcessingException() throws Exception {
        JwtUtils jwtUtils = new JwtUtils();
        setField(jwtUtils, "privateKey", "testSecretKey");
        setField(jwtUtils, "accessTokenExpirationMs", 10000L);
        setField(jwtUtils, "refreshTokenExpirationMs", 20000L);

        ObjectMapper mockMapper = mock(ObjectMapper.class);
        when(mockMapper.writeValueAsString(any())).thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("mock") {});
        setField(jwtUtils, "objectMapper", mockMapper);

        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", "user1");
        assertThrows(RuntimeException.class, () -> jwtUtils.encode(payload));
    }

    @Test
    void testDecode_InvalidToken() {
        assertThrows(IllegalArgumentException.class, () -> jwtUtils.decode("invalid.token"));
    }

    @Test
    void testDecode_IOException() {
        // 构造非法 payload base64
        String header = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9";
        String payload = "invalid_payload";
        String signature = "sig";
        String token = header + "." + payload + "." + signature;
        assertThrows(RuntimeException.class, () -> jwtUtils.decode(token));
    }

    @Test
    void testGenerateAccessTokenAndRefreshToken() {
        String accessToken = jwtUtils.generateAccessToken("user1", "admin");
        assertNotNull(accessToken);
        Map<String, Object> accessPayload = jwtUtils.decode(accessToken);
        assertEquals("user1", accessPayload.get("uid"));
        assertEquals("admin", accessPayload.get("identity"));
        assertTrue(accessPayload.containsKey("exp"));
        assertTrue(accessPayload.containsKey("iat"));

        String refreshToken = jwtUtils.generateRefreshToken("user2", "client", 3);
        assertNotNull(refreshToken);
        Map<String, Object> refreshPayload = jwtUtils.decode(refreshToken);
        assertEquals("user2", refreshPayload.get("uid"));
        assertEquals("client", refreshPayload.get("identity"));
        assertEquals(3, refreshPayload.get("ver"));
        assertTrue(refreshPayload.containsKey("exp"));
        assertTrue(refreshPayload.containsKey("iat"));
    }

    @Test
    void testRefreshToken() {
        String refreshToken = jwtUtils.generateRefreshToken("user3", "client", 1);
        Map<String, Object> oldPayload = jwtUtils.decode(refreshToken);
        String newToken = jwtUtils.refreshToken(refreshToken);
        Map<String, Object> newPayload = jwtUtils.decode(newToken);
        assertEquals(oldPayload.get("uid"), newPayload.get("uid"));
        assertEquals(oldPayload.get("identity"), newPayload.get("identity"));
        assertEquals(oldPayload.get("ver"), newPayload.get("ver"));
        assertTrue((Long)newPayload.get("exp") > (Long)oldPayload.get("exp"));
    }

    @Test
    void testSignException() {
        JwtUtils broken = new JwtUtils();
        setField(broken, "privateKey", null); // 触发 InvalidKeyException
        setField(broken, "accessTokenExpirationMs", 10000L);
        setField(broken, "refreshTokenExpirationMs", 20000L);
        Map<String, Object> payload = new HashMap<>();
        payload.put("uid", "user1");
        payload.put("identity", "admin");
        // 由于 null key，sign 时会抛异常
        assertThrows(RuntimeException.class, () -> broken.encode(payload));
    }
} 