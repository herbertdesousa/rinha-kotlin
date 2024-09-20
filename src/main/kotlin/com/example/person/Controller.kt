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

            if (repository.findOneByNickname(person.nickname) != null) {
                call.respond(HttpStatusCode.BadRequest, "Nickname in use")
            }

            val id = repository.create(
                PersonEntity(
                    person.name,
                    person.nickname,
                    LocalDate.parse(person.birthdate),
                    person.stack,
                )
            )

            call.response.headers.append("Location", "pessoas/${id}")
            call.respond(HttpStatusCode.Created, id)
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