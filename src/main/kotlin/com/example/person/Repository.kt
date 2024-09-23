package com.example.person

import com.example.common.Cache
import com.example.common.Database
import com.example.common.tables.People
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import java.time.LocalDate

data class PersonEntity(
    val name: String,
    val nickname: String,
    val birthdate: LocalDate,
    val stacks: List<String>
)

class Repository() {
    private val separator = "|:|"

    val cache = Cache<PersonEntity>(
        System.getenv("CACHE_PEOPLE_MAX_SIZE")?.toInt() ?: 100
    )

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    fun create(payload: PersonEntity): Int = Database.transaction {
        val peopleId = People.insert {
            it[name] = payload.name
            it[nickname] = payload.nickname
            it[birthdate] = payload.birthdate
            it[stacks] = serializeStacks(payload.stacks)
        }[People.id]

        cache.store(peopleId.toString(), payload)

        return@transaction peopleId
    }

    fun findOneById(id: Int): PersonEntity? {
        val foundCached = cache.getByKey(id.toString())

        if (foundCached != null) return foundCached

        return Database.transaction {
            People.select { People.id eq id }
                .map { PersonEntity(
                    it[People.name],
                    it[People.nickname],
                    it[People.birthdate],
                    deserializeStacks(it[People.stacks])
                ) }
                .singleOrNull()
        }
    }

    fun findOneByNickname(nickname: String): PersonEntity? {
        return Database.transaction {
            People.select { People.nickname eq nickname }
                .map { PersonEntity(
                    it[People.name],
                    it[People.nickname],
                    it[People.birthdate],
                    deserializeStacks(it[People.stacks])
                ) }
                .singleOrNull()
        }
    }

    fun queryByTerm(term: String) = Database.transaction {
        People.select {
            (People.name like "%$term%") or (People.nickname like "%$term%") or (People.stacks like "%$term%")
        }
            .limit(50)
            .map { PersonEntity(
                it[People.name],
                it[People.nickname],
                it[People.birthdate],
                deserializeStacks(it[People.stacks])
            ) }
    }

    fun count(): Long = Database.transaction {
        People.selectAll().count()
    }
}