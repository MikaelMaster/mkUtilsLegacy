package com.mikael.mkutilslegacy.api.redis

/**
 * Data used by [RedisAPI] to manage connections.
 *
 * @see RedisAPI.managerData
 */
class RedisConnectionData(

    var isEnabled: Boolean = false,

    var debug: Boolean = false,

    // RedisBungeeAPI - Start
    /**
     * @see RedisBungeeAPI
     */
    var useRedisBungeeAPI: Boolean = false,

    var debugRedisBungeeAPI: Boolean = false,
    // RedisBungeeAPI - End

    var usePass: Boolean = false,

    var pass: String = "password",

    var port: Int = 6379,

    var host: String = "localhost",

    var jedisPoolMaxClients: Int = 100,

    var jedisPoolMaxIdle: Int = 80,

    var jedisPoolMinIdle: Int = 50,

    var jedisPoolTestWhileIdle: Boolean = true,

    var jedisPoolMaxTimeout: Int = 3000
)