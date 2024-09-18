package com.example.person

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate

data class PersonEntity(
    val name: String,
    val nickname: String,
    val birthdate: LocalDate,
    val stacks: List<String>
)

class Repository(database: Database) {
    object People : Table("people") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 256)
        val nickname = varchar("nickname", length = 256).uniqueIndex()
        val birthdate = date("birthdate")
        val stacks = varchar("stack", length = 512)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(People)
        }
    }

    private val separator = "|:|"

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    suspend fun create(payload: PersonEntity): Int = dbQuery {
        People.insert {
            it[name] = payload.name
            it[nickname] = payload.nickname
            it[birthdate] = payload.birthdate
            it[stacks] = serializeStacks(payload.stacks)
        }[People.id]
    }

    suspend fun findById(id: Int): PersonEntity? {
        return dbQuery {
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
        return dbQuery {
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

    suspend fun queryByTerm(term: String) = dbQuery {
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

    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO) { block() }
}