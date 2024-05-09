package com.mikael.mkutilslegacy.api.redis

import com.mikael.mkutilslegacy.api.redis.RedisAPI.getExtraClient
import com.mikael.mkutilslegacy.api.redis.RedisAPI.getString
import com.mikael.mkutilslegacy.api.redis.RedisAPI.isInitialized
import com.mikael.mkutilslegacy.api.redis.RedisAPI.jedisPool
import com.mikael.mkutilslegacy.api.redis.RedisAPI.managerData
import com.mikael.mkutilslegacy.api.runTryCatch
import com.mikael.mkutilslegacy.spigot.UtilsMain
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool

/**
 * mkUtils [RedisAPI]
 *
 * Remember that to use this API you should mark 'RedisAPI.isEnabled' to True in [UtilsMain.config] file.
 *
 * To create a Redis Sub you should create a [Thread] and use [getExtraClient].
 * The [RedisConnectionData] required to connect an extra client is the [managerData].
 * Once you have an extra client inside the created [Thread], just register a Redis Sub using it.
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

    /**
     * Verify if a data exists in the redis server.
     *
     * To verify if multiple keys exists at the same time, use [existsAll].
     *
     * @param key the key to verify if the value exists.
     * @return True if the data exists. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun exists(key: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.exists(key)
            pipelined.sync()
            return response.get()
        }
    }

    /**
     * Verify if multiple keys exits in redis server.
     *
     * To verify if a single key exists use [exists].
     *
     * @param keys the keys to verify if them exists.
     * @return True if all given [keys] exists. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun existsAll(vararg keys: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.exists(*keys)
            pipelined.sync()
            return response.get() == keys.size.toLong()
        }
    }

    /**
     * Inserts a data into redis server using the given [key] and [value].
     *
     * @param key the key to push the value.
     * @param value the value to be pushed into redis server. The value will always be converted to string.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     * @see Jedis.set
     */
    fun insert(key: String, value: Any): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.set(key, value.toString())
                pipelined.sync()
            }
        }
    }

    /**
     * Inserts a map using the given [key] into redis server.
     *
     * @param key the key to push the value.
     * @param value the [Map] to be pushed into redis server.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     * @see Jedis.hset
     */
    fun insertMap(key: String, value: Map<String, String>): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.hset(key, value)
                pipelined.sync()
            }
        }
    }

    /**
     * Inserts a map using the given [key] into redis server.
     *
     * @param key the key to push the value.
     * @param mapKey the map key to be set.
     * @param mapValue the map value to be set.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     * @see Jedis.hset
     */
    fun insertMapValue(key: String, mapKey: String, mapValue: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.hsetnx(key, mapKey, mapValue)
                pipelined.sync()
            }
        }
    }

    /**
     * Inserts a String List into the redis server using the given [key] and [stringList].
     *
     * @param key the key to push the value.
     * @param stringList the String List to be pushed into redis server.
     * @param useExistingData if is to use the existing redis list data. If you want to send this list with just the given `stringList`, mark this as false.
     * @return True if the insert was completed. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun insertStringList(
        key: String,
        stringList: MutableList<String>,
        useExistingData: Boolean = true
    ): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            if (useExistingData) {
                val possibleData = getStringList(key)
                if (possibleData != null) {
                    for (string in possibleData) {
                        stringList.add(string)
                    }
                }
            }
            val stringBuilder = StringBuilder()
            for ((index, string) in stringList.withIndex()) {
                stringBuilder.append(string)
                if (index != stringList.lastIndex) stringBuilder.append(";")
            }
            insert(key, stringBuilder.toString())
        }
    }

    /**
     * Hmm ?
     */
    fun removeFromStringList(
        key: String,
        stringListToRemove: MutableList<String>
    ): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            val data = getStringList(key)?.toMutableList() ?: return@runTryCatch // There's nothing to remove
            if (data.isEmpty()) return@runTryCatch
            data.removeAll(stringListToRemove)
            insertStringList(key, data, false)
        }
    }

    /**
     * Get all data in redis liked with given [keys].
     *
     * This method is MUCH faster than using a For with [getString].
     *
     * @param keys the keys to get linked data from redis server.
     * @return all data for the given [keys]. If one of the given [keys] does not exits, 'nil' will be
     * returned as the value of that specific key.
     * @see Jedis.mget
     */
    fun getAllData(vararg keys: String): List<String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.mget(*keys)
            pipelined.sync()
            return response.get()
        }
    }

    /**
     * @see Jedis.hgetAll
     */
    fun getMap(key: String): Map<String, String> {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.hgetAll(key)
            pipelined.sync()
            return response.get()
        }
    }

    /**
     * @see Jedis.hget
     */
    fun getMapValue(key: String, mapKey: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.hget(key, mapKey)
            pipelined.sync()
            val value = response.get()
            return if (value != "nil") value else null
        }
    }

    /**
     * @see Jedis.hdel
     */
    fun mapDeleteAll(key: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.hdel(key)
                pipelined.sync()
            }
        }
    }

    /**
     * @see Jedis.hdel
     */
    fun mapDelete(key: String, mapKey: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.hdel(key, mapKey)
                pipelined.sync()
            }
        }
    }

    /**
     * Returns a String from redis server using the given [key].
     *
     * @param key the key to search on redis server for a data.
     * @return A String from the redis server. Null if the data does not exist.
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws NullPointerException if the returned data is null.
     */
    fun getString(key: String): String? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.get(key)
            pipelined.sync()
            val value = response.get()
            return if (value != "nil") value else null
        }
    }

    /**
     * Returns a String from redis server using the given [key].
     * If the returned data from redis is null, the given [defaultValue] will be set for the given [key],
     * and the [defaultValue] will be returned.
     *
     * @param key the key to search on redis server for a data.
     * @param defaultValue the value to associate with the [key] if there's no data for this [key] in redis server.
     * @return A String from the redis server, or the [defaultValue] if there's no data for the given [key].
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws NullPointerException if the returned data is null.
     */
    fun getOrPut(key: String, defaultValue: Any): String {
        if (!isInitialized) error("RedisAPI is not initialized.")
        jedisPool.resource.use { resource ->
            val pipelined = resource.pipelined()
            val response = pipelined.get(key)
            pipelined.sync()
            val value = response.get()
            if (value == "nil") {
                pipelined.set(key, "$defaultValue")
                pipelined.sync()
            }
            return value ?: "$defaultValue"
        }
    }

    /**
     * Returns a String List from redis server using the given [key].
     *
     * @param key the key to search on redis server for a data.
     * @return A String List from the redis server. Empty list of the data dos not exist.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun getStringList(key: String): List<String>? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return getString(key)?.split(";")?.filter { it.isNotBlank() }
    }

    /**
     * Returns an Int from redis server using the given [key].
     *
     * @param key the key to search on redis server for a data.
     * @return An Int? from the redis server. Null if the data does not exist.
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws NumberFormatException if the returned data is null or is not an Int.
     */
    fun getInt(key: String): Int? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return getString(key)?.toInt()
    }

    /**
     * Returns a Double from redis server using the given [key].
     *
     * @param key the key to search on redis server for a data.
     * @return A Double? from the redis server. Null if the data does not exist.
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws NumberFormatException if the returned data is null or is not an Int.
     */
    fun getDouble(key: String): Double? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return getString(key)?.toDouble()
    }

    /**
     * Returns a Long from redis server using the given [key].
     *
     * @param key the key to search on redis server for a data.
     * @return A Long? from the redis server. Null if the data does not exist.
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws NumberFormatException if the returned data is null or is not an Int.
     */
    fun getLong(key: String): Long? {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return getString(key)?.toLong()
    }

    /**
     * This will return a value for the given [key] transformed into the given [C] ([Any]).
     *
     * @param key the key to search on redis server for a data.
     * @return A Data as [C] from the redis server.
     * @throws IllegalStateException if [isInitialized] is false.
     * @throws ClassCastException if the returned value cannot be transformed into the given [C] ([Any]).
     */
    inline fun <reified C : Any> getDataAs(key: String): C {
        if (!isInitialized) error("RedisAPI is not initialized.")
        val data = getString(key)
        if (data !is C) error("Returned data '${data}' cannot be cast as the given custom class.")
        return data
    }

    /**
     * Deletes a data from redis server.
     *
     * @param key the key to delete from redis.
     * @return True if the value was successfully deleted. Otherwise, false.
     */
    fun delete(key: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                val pipelined = resource.pipelined()
                pipelined.del(key)
                pipelined.sync()
            }
        }
    }

    /**
     * Sends a ping to redis server.
     *
     * @return True if the ping is answered. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun testPing(): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                resource.ping()
            }
        }
    }

    /**
     * Sends an 'event' to redis server.
     *
     * @param channel the channel name to send the event.
     * @param message the message that will be sent with the event.
     * @return True if the event sent was completed. Otherwise, false.
     * @throws IllegalStateException if [isInitialized] is false.
     */
    fun sendEvent(channel: String, message: String): Boolean {
        if (!isInitialized) error("RedisAPI is not initialized.")
        return runTryCatch {
            jedisPool.resource.use { resource ->
                resource.publish(channel, message)
            }
        }
    }

    // Extra Client Section

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
        if (!connectionData.isEnabled) error("Given RedisConnectionData isEnabled should not be False.")
        val jedis = Jedis("http://${connectionData.host}:${connectionData.port}/")
        if (connectionData.usePass) {
            jedis.auth(connectionData.pass)
        }
        jedis.connect()
        return jedis
    }
}