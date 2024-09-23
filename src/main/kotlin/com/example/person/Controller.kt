package com.example.person

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
                    call.response.headers.append("Location", "pessoas/${result.id}")
                    call.respond(HttpStatusCode.Created, result.id)
                }
                CreatePersonResult.NicknameAlreadyInUse -> {
                    call.respond(HttpStatusCode.BadRequest, "Nickname in use")
                }
            }
        }

        get("/pessoas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val person = repository.findOneById(id)

            if (person != null) {
                println(person)
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