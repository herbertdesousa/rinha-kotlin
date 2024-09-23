package com.example.common

import com.example.common.tables.People
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.Transaction

object Database {
    lateinit var database: Database

    fun init(
        address: String,
        port: String,
        databaseName: String,
        usernameParam: String,
        passwordParam: String,
    ) {
        val config = HikariConfig().apply {
            jdbcUrl = "jdbc:postgresql://$address:$port/$databaseName"
            username = usernameParam
            password = passwordParam
            driverClassName = "org.postgresql.Driver"
            maximumPoolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 3
            transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name
        }

        database = Database.connect(
            datasource = HikariDataSource(config),
            databaseConfig = DatabaseConfig {
                defaultMaxAttempts = 5
            }
        )

        transaction {
            SchemaUtils.create(People)
        }
    }

    fun <T>transaction(
        statement: Transaction.() -> T
    ) = transaction(database, statement)
}