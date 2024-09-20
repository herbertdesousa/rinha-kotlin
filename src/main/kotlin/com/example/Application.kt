package com.example

import com.example.common.Database
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking

fun main() {
    runBlocking {
        Database.init(
            System.getenv("DB_ADDRESS") ?: "localhost",
            System.getenv("DB_PORT") ?: "5432",
            System.getenv("DB_DATABASE") ?: "postgres",
            System.getenv("DB_USERNAME") ?: "postgres",
            System.getenv("DB_PASSWORD") ?: "postgres",
        )
    }

    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureSerialization()
    configureRouting()
}
