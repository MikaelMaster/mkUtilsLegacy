package com.mikael.mkutilslegacy.api.redis

/**
 * Data used by [RedisAPI] to manage connections.
 *
 * @see RedisAPI.managerData
 */
class RedisConnectionData(

    var isEnabled: Boolean = false,

    /**
     * @see RedisBungeeAPI
     */
    var useRedisBungeeAPI: Boolean = false,

    var usePass: Boolean = false,

    var pass: String = "password",

    var port: Int = 6379,

    var host: String = "localhost",

    var jedisPoolMaxClients: Int = 50,

    var jedisPoolMaxIdle: Int = 50,

    var jedisPoolMinIdle: Int = 8,

    var jedisPoolTestWhileIdle: Boolean = true,

    var jedisPoolMaxTimeout: Int = 3000
)