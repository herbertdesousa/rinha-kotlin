package com.example.common.cache

import java.time.LocalDate
import java.util.HashMap

data class PersonCacheEntity(
    val name: String,
    val nickname: String,
    val birthdate: LocalDate,
    val stacks: List<String>
)

object PersonCache {
    private val separator = "|:|"

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    suspend fun store(id: String, data: PersonCacheEntity) {
        val model = HashMap<String, String>()
        model["name"] = data.name
        model["nickname"] = data.nickname
        model["birthdate"] = data.birthdate.toString()
        model["stacks"] = serializeStacks(data.stacks)

        Cache.hSet(id, model)
    }

    suspend fun get(id: String): PersonCacheEntity? {
        val result = Cache.hGet(id) ?: return null

        val birthdate = if (result["birthdate"] !== null) LocalDate.parse(result["birthdate"]) else LocalDate.now()

        val stacks = result["stacks"]?.let { deserializeStacks(it) } ?: emptyList()

        return PersonCacheEntity(
            name = result["name"] ?: "",
            nickname = result["nickname"] ?: "",
            birthdate = birthdate,
            stacks = stacks,
        )

    }
}