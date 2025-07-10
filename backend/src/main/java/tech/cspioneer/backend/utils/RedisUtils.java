package tech.cspioneer.backend.utils;

import com.redislabs.modules.rejson.JReJSON;
import com.redislabs.modules.rejson.Path;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import redis.clients.jedis.*;
import redis.clients.jedis.exceptions.JedisConnectionException;
import redis.clients.jedis.exceptions.JedisException;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;
import redis.clients.jedis.util.Pool;

import java.util.Map;

@Component
public class RedisUtils {

    private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);
    
    @Value("${spring.redis.host}")
    private String redisHost;
    
    @Value("${spring.redis.port}")
    private int redisPort;
    
    @Value("${spring.redis.password}")
    private String redisPassword;
    
    @Value("${spring.redis.timeout:2000}")
    private int redisTimeout;
    
    private static String server_address;
    private static int server_port;
    private static String server_password;
    private static int timeout;
    
    private static Pool<Jedis> jedisPool;
    
    @PostConstruct
    public void init() {
        server_address = redisHost;
        server_port = redisPort;
        server_password = redisPassword;
        timeout = redisTimeout;

        log.info("Initializing JedisPool with host: {}, port: {}, timeout: {}", server_address, server_port, timeout);
        
        // 初始化连接池
        JedisPoolConfig config = new JedisPoolConfig();
        // 在从连接池借用连接时，测试连接是否有效，防止拿到已关闭的连接
        config.setTestOnBorrow(true);

        if (server_password != null && !server_password.isEmpty()) {
            jedisPool = new JedisPool(config, server_address, server_port, timeout, server_password);
        } else {
            jedisPool = new JedisPool(config, server_address, server_port, timeout);
        }

        log.info("JedisPool initialized. Verifying connection to Redis...");
        try (Jedis jedis = jedisPool.getResource()) {
            String pong = jedis.ping();
            log.info("Successfully connected to Redis. Server responded with: {}", pong);
        } catch (JedisConnectionException e) {
            log.error("Failed to connect to Redis during initialization. Please check if your Redis server is running and accessible.", e);
        }
    }

    private RedisUtils() {} // Private constructor to prevent instantiation

    private static Jedis getJedis(int dbIndex) {
        Jedis jedis = jedisPool.getResource();
        if (server_password != null && !server_password.isEmpty()) {
            jedis.auth(server_password);
        }
        jedis.select(dbIndex);
        return jedis;
    }

    private static JReJSON getJReJSON() {
        // GetJReJSON doesn't need Jedis authentication or selection as it uses the JedisPool directly
        return new JReJSON(jedisPool);
    }

    // 设置/修改一个键值对,并设置过期时间
    public static void set(String key, String value, int seconds, int dbIndex) {
        log.debug("Attempting to set key '{}' in Redis DB {}", key, dbIndex);
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.set(key, value);
            jedis.expire(key, seconds);
            log.debug("Successfully set key '{}' in Redis DB {}", key, dbIndex);
        } catch (JedisException e) {
            log.error("Error setting key '{}' in Redis DB {}", key, dbIndex, e);
            throw e; // re-throw the exception to be caught by the service layer
        }
    }

    // 获取键值对
    public static String get(String key, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.get(key);
        }
    }

    // 删除键值对
    public static void del(String key, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.del(key);
        }
    }

    // 检查键是否存在
    public static boolean exists(String key, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.exists(key);
        }
    }

    // 返回所有键值对
    public static String[] keys(int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.keys("*").toArray(new String[0]);
        }
    }

    // 使用 RedisJSON 设置 JSON 数据
    public static void jsonSet(String key, String path, Object value, int dbIndex) {
        JReJSON jsonApi = getJReJSON();
        try (Jedis jedis = getJedis(dbIndex)) {
            jsonApi.set(key, value, new Path(path));
        }
    }

    // 使用 RedisJSON 获取 JSON 数据
    public static <T> T jsonGet(String key, String path, Class<T> clazz, int dbIndex) {
        JReJSON jsonApi = getJReJSON();
        try (Jedis jedis = getJedis(dbIndex)) {
            return jsonApi.get(key, clazz, new Path(path));
        }
    }

    // 使用 RedisJSON 删除 JSON 数据
    public static void jsonDel(String key, String path, int dbIndex) {
        JReJSON jsonApi = getJReJSON();
        try (Jedis jedis = getJedis(dbIndex)) {
            jsonApi.del(key, new Path(path));
        }
    }

    // 设置/修改一个 Hash 键值对
    public static void hset(String key, String field, String value, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.hset(key, field, value);
        }
    }

    // 获取一个 Hash 键值对
    public static String hget(String key, String field, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.hget(key, field);
        }
    }

    // 获取一个 Hash 的所有键值对
    public static Map<String, String> hgetAll(String key, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.hgetAll(key);
        }
    }

    // 删除一个 Hash 键值对
    public static void hdel(String key, String field, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.hdel(key, field);
        }
    }

    // 删除指定用户名的键值对
    public static void deleteByValue(String expectedUsername, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            String cursor = "0";
            ScanParams scanParams = new ScanParams().match("*").count(100);
            do {
                ScanResult<String> scanResult = jedis.scan(cursor, scanParams);
                cursor = scanResult.getCursor();

                for (String key : scanResult.getResult()) {
                    String value = jedis.get(key);
                    if (expectedUsername.equals(value)) {
                        jedis.del(key);
                        System.out.println("Deleted key: " + key + " with value: " + value);
                    }
                }
            } while (!"0".equals(cursor));
        }
    }

    // 添加 token 到黑名单
    public static void addToBlacklist(String token, int seconds, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            jedis.set(token, "blacklisted");
            jedis.expire(token, seconds);
        }
    }

    // 检查 token 是否在黑名单中
    public static boolean isTokenBlacklisted(String token, int dbIndex) {
        try (Jedis jedis = getJedis(dbIndex)) {
            return jedis.exists(token);
        }
    }
}