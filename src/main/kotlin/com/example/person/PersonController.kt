package com.example.person

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.personRoutes() {
    routing {
        post("/pessoas") {
            call.respond(HttpStatusCode.Created, "ok")
        }
    }
}