package tech.cspioneer.backend.utils;

import com.redislabs.modules.rejson.JReJSON;
import com.redislabs.modules.rejson.Path;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.exceptions.JedisException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisUtilsTest {

    private static JedisPool jedisPool;
    private static Jedis jedis;
    private static JReJSON jReJSON;

    @BeforeEach
    void setUp() throws Exception {
        jedisPool = mock(JedisPool.class);
        jedis = mock(Jedis.class);
        jReJSON = mock(JReJSON.class);

        // 反射注入静态字段
        java.lang.reflect.Field poolField = RedisUtils.class.getDeclaredField("jedisPool");
        poolField.setAccessible(true);
        poolField.set(null, jedisPool);

        java.lang.reflect.Field pwdField = RedisUtils.class.getDeclaredField("server_password");
        pwdField.setAccessible(true);
        pwdField.set(null, "");

        when(jedisPool.getResource()).thenReturn(jedis);
    }

    @Test
    void testSetAndGet() {
        when(jedis.set("k", "v")).thenReturn("OK");
        when(jedis.expire("k", 10)).thenReturn(1L);
        RedisUtils.set("k", "v", 10, 0);
        when(jedis.get("k")).thenReturn("v");
        assertEquals("v", RedisUtils.get("k", 0));
    }

    @Test
    void testSet_Exception() {
        when(jedis.set(anyString(), anyString())).thenThrow(new RuntimeException("err"));
        assertThrows(RuntimeException.class, () -> RedisUtils.set("k", "v", 10, 0));
    }

    @Test
    void testDelExistsKeys() {
        when(jedis.del("k")).thenReturn(1L);
        RedisUtils.del("k", 0);
        when(jedis.exists("k")).thenReturn(true);
        assertTrue(RedisUtils.exists("k", 0));
        when(jedis.keys("*")).thenReturn(new HashSet<>(Arrays.asList("a", "b")));
        assertArrayEquals(new String[]{"a", "b"}, RedisUtils.keys(0));
    }

    @Test
    void testJsonSetGetDel() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            doNothing().when(jReJSON).set(anyString(), any(), any(Path.class));
            RedisUtils.jsonSet("k", ".", "v", 0);
            when(jReJSON.get(anyString(), eq(String.class), any(Path.class))).thenReturn("v");
            assertEquals("v", RedisUtils.jsonGet("k", ".", String.class, 0));
            when(jReJSON.del(anyString(), any(Path.class))).thenReturn(1L);
            RedisUtils.jsonDel("k", ".", 0);
        }
    }

    @Test
    void testHashOps() {
        when(jedis.hset("k", "f", "v")).thenReturn(1L);
        RedisUtils.hset("k", "f", "v", 0);
        when(jedis.hget("k", "f")).thenReturn("v");
        assertEquals("v", RedisUtils.hget("k", "f", 0));
        Map<String, String> map = new HashMap<>();
        map.put("f", "v");
        when(jedis.hgetAll("k")).thenReturn(map);
        assertEquals(map, RedisUtils.hgetAll("k", 0));
        when(jedis.hdel("k", "f")).thenReturn(1L);
        RedisUtils.hdel("k", "f", 0);
    }

    @Test
    void testDeleteByValue() {
        ScanResult<String> scanResult = mock(ScanResult.class);
        when(scanResult.getCursor()).thenReturn("0");
        when(scanResult.getResult()).thenReturn(Arrays.asList("k1", "k2"));
        when(jedis.scan(anyString(), any(ScanParams.class))).thenReturn(scanResult);
        when(jedis.get("k1")).thenReturn("user1");
        when(jedis.get("k2")).thenReturn("user2");
        when(jedis.del(anyString())).thenReturn(1L);
        RedisUtils.deleteByValue("user1", 0);
    }

    @Test
    void testBlacklist() {
        when(jedis.set("token", "blacklisted")).thenReturn("OK");
        when(jedis.expire("token", 10)).thenReturn(1L);
        RedisUtils.addToBlacklist("token", 10, 0);
        when(jedis.exists("token")).thenReturn(true);
        assertTrue(RedisUtils.isTokenBlacklisted("token", 0));
    }

    @Test
    void testSet_JedisException() {
        when(jedis.set(anyString(), anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.set("k", "v", 10, 0));
    }

    @Test
    void testGet_Exception() {
        when(jedis.get(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.get("k", 0));
    }

    @Test
    void testDel_Exception() {
        when(jedis.del(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.del("k", 0));
    }

    @Test
    void testExists_Exception() {
        when(jedis.exists(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.exists("k", 0));
    }

    @Test
    void testKeys_Exception() {
        when(jedis.keys(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.keys(0));
    }

    @Test
    void testJsonSet_Exception() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            doThrow(new JedisException("fail")).when(jReJSON).set(anyString(), any(), any(Path.class));
            assertThrows(JedisException.class, () -> RedisUtils.jsonSet("k", ".", "v", 0));
        }
    }

    @Test
    void testJsonGet_Exception() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            when(jReJSON.get(anyString(), eq(String.class), any(Path.class))).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonGet("k", ".", String.class, 0));
        }
    }

    @Test
    void testJsonDel_Exception() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            when(jReJSON.del(anyString(), any(Path.class))).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonDel("k", ".", 0));
        }
    }

    @Test
    void testHashOps_Exception() {
        when(jedis.hset(anyString(), anyString(), anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.hset("k", "f", "v", 0));
        when(jedis.hget(anyString(), anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.hget("k", "f", 0));
        when(jedis.hgetAll(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.hgetAll("k", 0));
        when(jedis.hdel(anyString(), anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.hdel("k", "f", 0));
    }

    @Test
    void testDeleteByValue_NoMatch() {
        ScanResult<String> scanResult = mock(ScanResult.class);
        when(scanResult.getCursor()).thenReturn("0");
        when(scanResult.getResult()).thenReturn(Collections.singletonList("k1"));
        when(jedis.scan(anyString(), any(ScanParams.class))).thenReturn(scanResult);
        when(jedis.get("k1")).thenReturn("other");
        RedisUtils.deleteByValue("user1", 0); // 不会del
    }

    @Test
    void testDeleteByValue_MultiScan() {
        ScanResult<String> scan1 = mock(ScanResult.class);
        ScanResult<String> scan2 = mock(ScanResult.class);
        when(scan1.getCursor()).thenReturn("1");
        when(scan1.getResult()).thenReturn(Arrays.asList("k1"));
        when(scan2.getCursor()).thenReturn("0");
        when(scan2.getResult()).thenReturn(Arrays.asList("k2"));
        when(jedis.scan(eq("0"), any(ScanParams.class))).thenReturn(scan1);
        when(jedis.scan(eq("1"), any(ScanParams.class))).thenReturn(scan2);
        when(jedis.get(anyString())).thenReturn("user1");
        when(jedis.del(anyString())).thenReturn(1L);
        RedisUtils.deleteByValue("user1", 0);
    }

    @Test
    void testDeleteByValue_GetException() {
        ScanResult<String> scanResult = mock(ScanResult.class);
        when(scanResult.getCursor()).thenReturn("0");
        when(scanResult.getResult()).thenReturn(Collections.singletonList("k1"));
        when(jedis.scan(anyString(), any(ScanParams.class))).thenReturn(scanResult);
        when(jedis.get("k1")).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.deleteByValue("user1", 0));
    }

    @Test
    void testAddToBlacklist_Exception() {
        when(jedis.set(anyString(), anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.addToBlacklist("token", 10, 0));
    }

    @Test
    void testIsTokenBlacklisted_Exception() {
        when(jedis.exists(anyString())).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.isTokenBlacklisted("token", 0));
    }

    @Test
    void testSet_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.set("k", "v", 10, 0));
    }

    @Test
    void testGet_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.get("k", 0));
    }

    @Test
    void testDel_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.del("k", 0));
    }

    @Test
    void testExists_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.exists("k", 0));
    }

    @Test
    void testKeys_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.keys(0));
    }

    @Test
    void testKeys_ReturnNull() {
        when(jedis.keys("*")).thenReturn(null);
        assertThrows(NullPointerException.class, () -> RedisUtils.keys(0));
    }

    @Test
    void testJsonSet_JedisPoolException() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonSet("k", ".", "v", 0));
        }
    }

    @Test
    void testJsonSet_GetJReJSON_Exception() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonSet("k", ".", "v", 0));
        }
    }

    @Test
    void testJsonGet_JedisPoolException() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonGet("k", ".", String.class, 0));
        }
    }

    @Test
    void testJsonDel_JedisPoolException() {
        try (MockedStatic<RedisUtils> staticMock = Mockito.mockStatic(RedisUtils.class, Mockito.CALLS_REAL_METHODS)) {
            staticMock.when(RedisUtils::getJReJSON).thenReturn(jReJSON);
            when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
            assertThrows(JedisException.class, () -> RedisUtils.jsonDel("k", ".", 0));
        }
    }

    @Test
    void testHashOps_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.hset("k", "f", "v", 0));
        assertThrows(JedisException.class, () -> RedisUtils.hget("k", "f", 0));
        assertThrows(JedisException.class, () -> RedisUtils.hgetAll("k", 0));
        assertThrows(JedisException.class, () -> RedisUtils.hdel("k", "f", 0));
    }

    @Test
    void testDeleteByValue_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.deleteByValue("user1", 0));
    }

    @Test
    void testDeleteByValue_ScanResultNull() {
        ScanResult<String> scanResult = mock(ScanResult.class);
        when(scanResult.getCursor()).thenReturn("0");
        when(scanResult.getResult()).thenReturn(null);
        when(jedis.scan(anyString(), any(ScanParams.class))).thenReturn(scanResult);
        assertThrows(NullPointerException.class, () -> RedisUtils.deleteByValue("user1", 0));
    }

    @Test
    void testDeleteByValue_NullExpectedUsername() {
        ScanResult<String> scanResult = mock(ScanResult.class);
        when(scanResult.getCursor()).thenReturn("0");
        when(scanResult.getResult()).thenReturn(Arrays.asList("k1"));
        when(jedis.scan(anyString(), any(ScanParams.class))).thenReturn(scanResult);
        when(jedis.get("k1")).thenReturn("user1");
        assertThrows(NullPointerException.class, () -> RedisUtils.deleteByValue(null, 0));
    }

    @Test
    void testAddToBlacklist_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.addToBlacklist("token", 10, 0));
    }

    @Test
    void testIsTokenBlacklisted_JedisPoolException() {
        when(jedisPool.getResource()).thenThrow(new JedisException("fail"));
        assertThrows(JedisException.class, () -> RedisUtils.isTokenBlacklisted("token", 0));
    }

    @Test
    void testGet_NullKey() {
        when(jedis.get((String) null)).thenReturn(null);
        assertNull(RedisUtils.get(null, 0));
    }

    @Test
    void testHget_NullField() {
        when(jedis.hget("k", null)).thenReturn(null);
        assertNull(RedisUtils.hget("k", null, 0));
    }

    @Test
    void testPrivateConstructor() throws Exception {
        java.lang.reflect.Constructor<RedisUtils> constructor = RedisUtils.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        constructor.newInstance();
    }
} 