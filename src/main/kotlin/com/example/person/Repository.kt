package com.example.person

import com.example.common.Database
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import java.time.LocalDate

data class PersonEntity(
    val name: String,
    val nickname: String,
    val birthdate: LocalDate,
    val stacks: List<String>
)

class Repository(database: Database) {
    private val separator = "|:|"

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    suspend fun create(payload: PersonEntity): Int = dbQuery {
        Database.People.insert {
            it[name] = payload.name
            it[nickname] = payload.nickname
            it[birthdate] = payload.birthdate
            it[stacks] = serializeStacks(payload.stacks)
        }[Database.People.id]
    }

    suspend fun findOneById(id: Int): PersonEntity? {
        return dbQuery {
            Database.People.select { Database.People.id eq id }
                .map { PersonEntity(
                    it[Database.People.name],
                    it[Database.People.nickname],
                    it[Database.People.birthdate],
                    deserializeStacks(it[Database.People.stacks])
                ) }
                .singleOrNull()
        }
    }

    suspend fun findOneByNickname(nickname: String): PersonEntity? {
        return dbQuery {
            Database.People.select { Database.People.nickname eq nickname }
                .map { PersonEntity(
                    it[Database.People.name],
                    it[Database.People.nickname],
                    it[Database.People.birthdate],
                    deserializeStacks(it[Database.People.stacks])
                ) }
                .singleOrNull()
        }
    }

    suspend fun queryByTerm(term: String) = dbQuery {
        Database.People.select {
            (Database.People.name like "%$term%") or (Database.People.nickname like "%$term%") or (Database.People.stacks like "%$term%")
        }
            .limit(50)
            .map { PersonEntity(
                it[Database.People.name],
                it[Database.People.nickname],
                it[Database.People.birthdate],
                deserializeStacks(it[Database.People.stacks])
            ) }
    }

    suspend fun count(): Long = dbQuery {
        Database.People.selectAll().count()
    }

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}