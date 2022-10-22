package com.mikael.mkutilslegacy.api.redis

import redis.clients.jedis.Jedis

class RedisConnectionData(

    /**
     * Data to create a redis client ([Jedis]) using the [RedisAPI].
     *
     * @see Jedis
     * @see RedisAPI.usedRedisConnectionData
     * @see RedisAPI
     */

    var isEnabled: Boolean = false,
    var syncBungeeDataUsingRedis: Boolean = false,
    var usePass: Boolean = false,
    var pass: String = "password",
    var port: Int = 6379,
    var host: String = "localhost"
)