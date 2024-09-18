package com.example.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.Database
import java.time.LocalDate

fun Application.personRoutes() {
    routing {
        val database = Database.connect(
            url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
            user = "root",
            driver = "org.h2.Driver",
            password = "",
        )

        val repository = Repository(database)

        post("/pessoas") {
            val person = call.receive<PersonDTO>()

            val id = repository.create(
                PersonEntity(
                    person.name,
                    person.nickname,
                    LocalDate.parse(person.birthdate),
                    person.stack,
                )
            )

            call.respond(HttpStatusCode.Created, id)
        }

        get("/pessoas/{id}") {
            val id = call.parameters["id"]?.toInt() ?: throw IllegalArgumentException("Invalid ID")

            val person = repository.findById(id)

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
    }
}