package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.bungee.UtilsBungeeMain
import com.mikael.mkutilslegacy.bungee.api.utilsBungeeMain
import com.mikael.mkutilslegacy.spigot.UtilsMain
import com.mikael.mkutilslegacy.spigot.api.utilsMain
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

/**
 * mkUtils [RedisAPI]
 *
 * This API is used to connect to a Redis server and perform operations like *set*, *get*, *del*, and more easily.
 *
 * Remember that to use this API you should mark *RedisAPI.isEnabled* to *true*
 * in [UtilsMain.config] (or [UtilsBungeeMain.config] if you are using BungeeCord) file.
 *
 * @author Mikael
 * @see RedisConnectionData
 * @see Jedis
 * @see JedisPool
 */
@Suppress("WARNINGS")
object RedisAPI {

    // Properties - Start

    // Connection data for the Redis server
    lateinit var managerData: RedisConnectionData

    // Configuration for the Jedis pool
    lateinit var jedisPoolConfig: GenericObjectPoolConfig<Jedis>

    // Pool of Jedis instances
    lateinit var jedisPool: JedisPool

    // RedisBungeeAPI - Start

    // Private variable to store the state of RedisBungeeAPI usage
    private var _useRedisBungeeAPI = false

    // Public property to get and set the state of RedisBungeeAPI usage
    var useRedisBungeeAPI: Boolean
        get() = _useRedisBungeeAPI
        internal set(value) {
            _useRedisBungeeAPI = value
        }
    // RedisBungeeAPI - End

    // Check if the Jedis pool and its configuration are initialized
    val isInitialized: Boolean get() = this::jedisPoolConfig.isInitialized && this::jedisPool.isInitialized
    // Properties - End

    // Internal methods - Start

    // Method to initialize and load the Redis API
    internal fun onEnableLoadRedisAPI(): Boolean {
        val config = GenericObjectPoolConfig<Jedis>()
        config.maxTotal = managerData.jedisPoolMaxClients
        config.maxIdle = managerData.jedisPoolMaxIdle
        config.minIdle = managerData.jedisPoolMinIdle
        config.testWhileIdle = managerData.jedisPoolTestWhileIdle
        jedisPoolConfig = config
        jedisPool = JedisPool(
            jedisPoolConfig,
            managerData.host,
            managerData.port,
            managerData.jedisPoolMaxTimeout,
            managerData.pass
        )
        jedisPool.preparePool()
        return true
    }

    // Method to unload the Redis API
    internal fun onDisableUnloadRedisAPI() {
        if (!isInitialized) return
        // jedisPool.destroy() // Não é necessário e não pode.
    }

    // Method to log debug messages
    private fun debug(msg: String) {
        if (!isInitialized || !managerData.debug) return
        if (isProxyServer) {
            utilsBungeeMain.log("§e[RedisAPI] §6[DEBUG] §f$msg")
        } else {
            utilsMain.log("§e[RedisAPI] §6[DEBUG] §f$msg")
        }
    }
    // Internal methods - End

    /**
     * Method to set a value in Redis
     * @see Jedis.set
     */
    fun set(key: String, value: Any): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.set(key, value.toString())
            debug("set() to '$key' took ${System.currentTimeMillis() - start}ms.")
            return res == "OK"
        }
    }

    /**
     * Method to set a map in Redis
     * @see Jedis.hset
     */
    fun setMap(key: String, value: Map<String, String>): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.hset(key, value)
            debug("setMap() to '$key' took ${System.currentTimeMillis() - start}ms.")
            return res
        }
    }

    /**
     * Method to set a value in a map in Redis
     * @see Jedis.hset
     */
    fun setMapValue(key: String, mapKey: String, mapValue: String): Long {
        return setMap(key, mapOf(mapKey to mapValue))
    }

    /**
     * Data class to hold the response of the exists method
     * @see exists
     */
    data class ExistsResponse(val verifiedKeys: Long, val existentKeys: Long) {
        /**
         * @return True if the verified keys are equals to the existent keys. Otherwise, false.
         */
        val asBoolean get() = verifiedKeys == existentKeys
    }

    /**
     * Method to check if keys exist in Redis
     * @return an [ExistsResponse] with the verified keys and the existent keys.
     * @see Jedis.exists
     */
    fun exists(vararg keys: String): ExistsResponse {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val verifiedKeys = keys.size.toLong()
            val existentKeys = resource.exists(*keys)
            debug("exists() took ${System.currentTimeMillis() - start}ms. Verified: $verifiedKeys | Existent: ${existentKeys}.")
            return ExistsResponse(verifiedKeys, existentKeys)
        }
    }

    /**
     * Method to get all data for given keys from Redis
     * @see Jedis.mget
     */
    fun getAllData(vararg keys: String): List<String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.mget(*keys)
            debug("getAllData() took ${System.currentTimeMillis() - start}ms.")
            return res
        }
    }

    /**
     * Method to get a string value from Redis
     * @see Jedis.get
     */
    fun getString(key: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.get(key)
            debug("getString() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return if (res != "nil") res else null
        }
    }

    // Method to get an integer value from Redis
    fun getInt(key: String): Int? {
        return getString(key)?.toInt()
    }

    // Method to get a double value from Redis
    fun getDouble(key: String): Double? {
        return getString(key)?.toDouble()
    }

    // Method to get a long value from Redis
    fun getLong(key: String): Long? {
        return getString(key)?.toLong()
    }

    /**
     * Method to get a map from Redis
     * @see Jedis.hgetAll
     */
    fun getMap(key: String): Map<String, String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.hgetAll(key)
            debug("getMap() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return res
        }
    }

    /**
     * Method to get a value from a map in Redis
     * @see Jedis.hget
     */
    fun getMapValue(key: String, mapKey: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.hget(key, mapKey)
            debug("getMapValue() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return if (res != "nil") res else null
        }
    }

    /**
     * Method to delete keys from Redis
     * @see Jedis.del
     */
    fun delete(vararg keys: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.del(*keys)
            debug("delete() took ${System.currentTimeMillis() - start}ms. Deleted: $res.")
            return res
        }
    }

    /**
     * Method to delete keys from a map in Redis
     * @see Jedis.hdel
     */
    fun mapDelete(key: String, vararg mapKeys: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.hdel(key, *mapKeys)
            debug("mapDelete() from '$key' took ${System.currentTimeMillis() - start}ms. Deleted: $res.")
            return res
        }
    }

    /**
     * Method to publish a message to a channel in Redis
     * @see Jedis.publish
     */
    fun sendEvent(channel: String, message: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val res = resource.publish(channel, message)
            debug("sendEvent() to '$channel' took ${System.currentTimeMillis() - start}ms. Received by: ${res} pub-subs.")
            return res
        }
    }

    // Method to get an extra client for a given connection data
    fun getExtraClient(connectionData: RedisConnectionData): Jedis {
        if (!connectionData.isEnabled) error("Given RedisConnectionData isEnabled should not be false.")
        val start = System.currentTimeMillis()
        val jedis = Jedis("http://${connectionData.host}:${connectionData.port}/")
        if (connectionData.usePass) {
            jedis.auth(connectionData.pass)
        }
        jedis.connect()
        debug("getExtraClient() took ${System.currentTimeMillis() - start}ms. UsePass: ${connectionData.usePass}.")
        return jedis
    }
}