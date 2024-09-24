package com.example.person

import com.example.common.cache.NicknameCache
import com.example.common.cache.PersonCache
import com.example.common.cache.PersonCacheEntity
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate

fun Application.personRoutes() {
    routing {
        val repository = Repository()

        post("/pessoas") {
            val person = call.receive<PersonDTO>()

            if (NicknameCache.exists(person.apelido)) {
                call.respond(HttpStatusCode.BadRequest, "Nickname in use")
            }

            val result = repository.create(
                PersonEntity(
                    person.nome,
                    person.apelido,
                    LocalDate.parse(person.nascimento),
                    person.stack,
                )
            )

            when (result) {
                is CreatePersonResult.Success -> {
                    NicknameCache.store(person.apelido)
                    PersonCache.store(
                        result.id.toString(),
                        PersonCacheEntity(
                            person.nome,
                            person.apelido,
                            LocalDate.parse(person.nascimento),
                            person.stack,
                        )
                    )

                    call.response.headers.append("Location", "pessoas/${result.id}")
                    call.respond(HttpStatusCode.Created, result.id)
                }

                CreatePersonResult.NicknameAlreadyInUse -> {
                    call.respond(HttpStatusCode.BadRequest, "Nickname in use")

                    NicknameCache.store(person.apelido)
                }
            }
        }

        get("/pessoas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val userCached = PersonCache.get(id.toString())

            if (userCached !== null) {
                call.respond(
                    HttpStatusCode.OK,
                    PersonDTO(
                        userCached.name,
                        userCached.nickname,
                        userCached.birthdate.toString(),
                        userCached.stacks,
                    )
                )
            }

            val person = repository.findOneById(id)

            if (person != null) {
                call.respond(
                    HttpStatusCode.OK,
                    PersonDTO(
                        person.name,
                        person.nickname,
                        person.birthdate.toString(),
                        person.stacks,
                    )
                )
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        get("/pessoas") {
            val term = call.queryParameters["t"] ?: call.respond(HttpStatusCode.BadRequest, "Term required")

            val people = repository.queryByTerm(term.toString())

            call.respond(
                HttpStatusCode.OK,
                people.map {
                    PersonDTO(
                        it.name,
                        it.nickname,
                        it.birthdate.toString(),
                        it.stacks,
                    )
                }
            )
        }

        get("/contagem-pessoas") {
            call.respond(HttpStatusCode.OK, repository.count())
        }
    }
}