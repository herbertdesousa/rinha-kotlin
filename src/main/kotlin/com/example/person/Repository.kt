package com.example.person

import com.example.common.Database
import com.example.common.tables.People
import kotlinx.coroutines.Dispatchers
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

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    suspend fun create(payload: PersonEntity): Int = Database.transaction {
        People.insert {
            it[name] = payload.name
            it[nickname] = payload.nickname
            it[birthdate] = payload.birthdate
            it[stacks] = serializeStacks(payload.stacks)
        }[People.id]
    }

    suspend fun findOneById(id: Int): PersonEntity? {
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

    suspend fun findOneByNickname(nickname: String): PersonEntity? {
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

    suspend fun queryByTerm(term: String) = Database.transaction {
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

    suspend fun count(): Long = Database.transaction {
        People.selectAll().count()
    }
}