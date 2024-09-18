package com.example.plugins

import com.example.person.personRoutes
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    personRoutes()

    routing {
        get("/") {
            call.respondText("Hello World!")
        }
    }
}
