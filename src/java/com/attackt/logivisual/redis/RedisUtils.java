package com.attackt.logivisual.redis;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * redis 工具类
 */
public enum RedisUtils {
    INSTANCE;
    private JedisPool jedisPool;

    Config config = ConfigFactory.load();

    RedisUtils() {
        // 连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(config.getInt("redis.maxTota"));// 最大连接数
        jedisPoolConfig.setMaxIdle(config.getInt("redis.maxIdle"));// 最大空闲连接数
        jedisPoolConfig.setMaxWaitMillis(1000 * config.getInt("redis.maxWaitMillis"));// 获取连接最大等等时间
        jedisPoolConfig.setTestOnBorrow(config.getBoolean("redis.testOnBorrow"));// 获取连接的时检查有效性

        String ip = config.getString("redis.ip");
        int port = config.getInt("redis.port");
        int timeout = config.getInt("redis.timeout");// 连接超时时间
        jedisPool = new JedisPool(jedisPoolConfig, ip, port, timeout);
    }

    public Jedis getJedis() {
        Jedis jedis = jedisPool.getResource();
        return jedis;
    }

    public String set(String key, String value) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        } finally {
            releaseResource(jedis);
        }
    }

    /**
     * 保存字符串，没有返回null
     * @param key
     * @return
     */
    public String get(String key) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
            return "-1";
        } finally {
            releaseResource(jedis);
        }
    }

    /**
     * 拼接,返回拼接后的长度
     * @param key 目标的key
     * @param value 要接在后面的value
     * @return
     */
    public Long append(String key, String value) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.append(key, value);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            releaseResource(jedis);
        }
    }

    /**
     * 保存字符串并设置保存有效期,成功返回OK
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public String setex(String key, String value, int seconds) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.setex(key, seconds, value);
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            releaseResource(jedis);
        }
    }

    /**
     * 清空当前库下的数据
     * @return
     */
    public String flushDB() {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.flushDB();
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        } finally {
            releaseResource(jedis);
        }
    }

    /**
     * 判断Key是否存在
     * @param key
     * @return
     */
    public Boolean exists(String key) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(key);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            releaseResource(jedis);
        }
    }


    /**
     * 判断多个Key是否存在,返回存在的数量
     * @param keys
     * @return
     */
    public Long exists(String... keys) {

        Jedis jedis = null;
        try {
            jedis = getJedis();
            return jedis.exists(keys);
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        } finally {
            releaseResource(jedis);
        }
    }


    public  void releaseResource(Jedis jedis) {
        if (jedis != null) {
            jedis.close();// 资源回收
        }
    }
}
