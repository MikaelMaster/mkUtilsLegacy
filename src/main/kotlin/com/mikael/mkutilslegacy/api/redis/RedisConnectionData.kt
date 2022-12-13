package com.mikael.mkutilslegacy.api.redis

import redis.clients.jedis.Jedis

/**
 * Data to create a redis client ([Jedis]) using the [RedisAPI].
 *
 * @see Jedis
 * @see RedisAPI.usedRedisConnectionData
 * @see RedisAPI
 */
class RedisConnectionData(

    var isEnabled: Boolean = false,

    /**
     * @see RedisBungeeAPI
     */
    var syncBungeeDataUsingRedis: Boolean = false,

    var usePass: Boolean = false,

    var pass: String = "password",

    var port: Int = 6379,

    var host: String = "localhost"

)