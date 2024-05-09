package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.isProxyServer
import com.mikael.mkutilslegacy.api.redis.RedisAPI.jedisPool
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
    lateinit var managerData: RedisConnectionData

    lateinit var jedisPoolConfig: GenericObjectPoolConfig<Jedis>
    lateinit var jedisPool: JedisPool

    // RedisBungeeAPI - Start
    private var _useRedisBungeeAPI = false
    var useRedisBungeeAPI: Boolean
        get() = _useRedisBungeeAPI
        internal set(value) {
            _useRedisBungeeAPI = value
        }
    // RedisBungeeAPI - End

    val isInitialized: Boolean get() = this::jedisPoolConfig.isInitialized && this::jedisPool.isInitialized
    // Properties - End

    // Internal methods - Start
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
        return true
    }

    internal fun onDisableUnloadRedisAPI() {
        if (!isInitialized) return
        jedisPool.destroy()
    }

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
     * @see Jedis.set
     */
    fun set(key: String, value: Any): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.set(key, value.toString())
            pipelined.sync()
            debug("set() to '$key' took ${System.currentTimeMillis() - start}ms.")
            return res.get() == "OK"
        }
    }

    /**
     * @see Jedis.hset
     */
    fun setMap(key: String, value: Map<String, String>): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.hset(key, value)
            pipelined.sync()
            debug("setMap() to '$key' took ${System.currentTimeMillis() - start}ms.")
            return res.get()
        }
    }

    /**
     * @see Jedis.hsetnx
     */
    fun setMapValue(key: String, mapKey: String, mapValue: String): Long {
        return setMap(key, mapOf(mapKey to mapValue))
    }

    /**
     * @see exists
     */
    data class ExistsResponse(val verifiedKeys: Long, val existentKeys: Long) {
        /**
         * @return True if the verified keys are equals to the existent keys. Otherwise, false.
         */
        val asBoolean get() = verifiedKeys == existentKeys
    }

    /**
     * @return an [ExistsResponse] with the verified keys and the existent keys.
     * @see Jedis.exists
     */
    fun exists(vararg keys: String): ExistsResponse {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.exists(*keys)
            pipelined.sync()
            val verifiedKeys = keys.size.toLong()
            val existentKeys = response.get()
            debug("exists() took ${System.currentTimeMillis() - start}ms. Verified: $verifiedKeys | Existent: ${existentKeys}.")
            return ExistsResponse(verifiedKeys, existentKeys)
        }
    }

    /**
     * @see Jedis.mget
     */
    fun getAllData(vararg keys: String): List<String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.mget(*keys)
            pipelined.sync()
            debug("getAllData() took ${System.currentTimeMillis() - start}ms.")
            return res.get()
        }
    }

    /**
     * @see Jedis.get
     */
    fun getString(key: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.get(key)
            pipelined.sync()
            val value = res.get()
            debug("getString() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return if (value != "nil") value else null
        }
    }

    fun getInt(key: String): Int? {
        return getString(key)?.toInt()
    }

    fun getDouble(key: String): Double? {
        return getString(key)?.toDouble()
    }

    fun getLong(key: String): Long? {
        return getString(key)?.toLong()
    }

    /**
     * @see Jedis.hgetAll
     */
    fun getMap(key: String): Map<String, String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.hgetAll(key)
            pipelined.sync()
            debug("getMap() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return res.get()
        }
    }

    /**
     * @see Jedis.hget
     */
    fun getMapValue(key: String, mapKey: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.hget(key, mapKey)
            pipelined.sync()
            val value = res.get()
            debug("getMapValue() from '$key' took ${System.currentTimeMillis() - start}ms.")
            return if (value != "nil") value else null
        }
    }


    /**
     * @see Jedis.del
     */
    fun delete(vararg keys: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.del(*keys)
            pipelined.sync()
            val deletedCount = res.get()
            debug("delete() took ${System.currentTimeMillis() - start}ms. Deleted: $deletedCount.")
            return deletedCount
        }
    }

    /**
     * @see Jedis.hdel
     */
    fun mapDelete(key: String, vararg mapKeys: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.hdel(key, *mapKeys)
            pipelined.sync()
            val deletedCount = res.get()
            debug("mapDelete() from '$key' took ${System.currentTimeMillis() - start}ms. Deleted: $deletedCount.")
            return deletedCount
        }
    }

    /**
     * @see Jedis.publish
     */
    fun sendEvent(channel: String, message: String): Long {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val start = System.currentTimeMillis()
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val res = pipelined.publish(channel, message)
            pipelined.sync()
            val receivedCount = res.get()
            debug("sendEvent() to '$channel' took ${System.currentTimeMillis() - start}ms. Received: ${receivedCount}.")
            return receivedCount
        }
    }

    /**
     * Creates a new [Jedis] and connect it using the given [RedisConnectionData].
     * This is usefully to create pub-subs.
     *
     * This method does NOT get a [Jedis] from the [jedisPool], this will create a new [Jedis] instance,
     * and then connect it using the given [connectionData] properties.
     *
     * @param connectionData A [RedisConnectionData] to create a new [Jedis].
     * @return A new connected [Jedis].
     * @throws IllegalStateException if the [RedisConnectionData.isEnabled] of the given [connectionData] is false.
     */
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