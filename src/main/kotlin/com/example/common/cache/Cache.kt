package com.example.common.cache

import io.lettuce.core.ExperimentalLettuceCoroutinesApi
import io.lettuce.core.KeyValue
import io.lettuce.core.RedisClient
import io.lettuce.core.api.coroutines
import io.lettuce.core.api.coroutines.RedisCoroutinesCommands
import kotlinx.coroutines.flow.singleOrNull
import kotlinx.coroutines.flow.toList
import java.util.HashMap

@OptIn(ExperimentalLettuceCoroutinesApi::class)
object Cache {
    private lateinit var client: RedisCoroutinesCommands<String, String>

    fun init(url: String) {
        client = RedisClient.create(url).connect().coroutines()
    }

    suspend fun hSet(key: String, data: Map<String, String>) {
        client.hset(key, data)
    }

    //
    suspend fun hGet(key: String): Map<String, String>? {
        val flow = client.hgetall(key)

        val list = flow.toList()

        if (list.isEmpty()) return null

        val map = HashMap<String, String>()

        list.map {
            map.put(it.key, it.value)
        }

        return map
    }

    suspend fun sAdd(collection: String, keyValue: String) {
        client.sadd(collection, keyValue)
    }

    suspend fun sCheckExists(collection: String, keyValue: String): Boolean? {
        return client.sismember(collection, keyValue)
    }
}