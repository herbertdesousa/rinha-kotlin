package com.example.person

import com.example.common.Database
import com.example.common.tables.People
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.postgresql.util.PSQLException
import java.time.LocalDate

data class PersonEntity(
    val name: String,
    val nickname: String,
    val birthdate: LocalDate,
    val stacks: List<String>
)

sealed class CreatePersonResult {
    class Success(val id: Int) : CreatePersonResult()
    data object NicknameAlreadyInUse : CreatePersonResult()
}

class Repository() {
    private val separator = "|:|"

    private fun serializeStacks(stacks: List<String>): String = stacks.joinToString(separator)

    private fun deserializeStacks(stacks: String): List<String> = stacks.split(separator)

    suspend fun create(payload: PersonEntity): CreatePersonResult = Database.transaction {
        try {
            val peopleId = People.insert {
                it[name] = payload.name
                it[nickname] = payload.nickname
                it[birthdate] = payload.birthdate
                it[stacks] = serializeStacks(payload.stacks)
            }[People.id]

            return@transaction CreatePersonResult.Success(peopleId)
        } catch(e: ExposedSQLException) {
            val cause = e.cause
            if (cause is PSQLException) {
                if (cause.serverErrorMessage?.message?.contains("duplicate key value violates unique constraint") == true)
                return@transaction CreatePersonResult.NicknameAlreadyInUse
            }

            throw e
        }
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