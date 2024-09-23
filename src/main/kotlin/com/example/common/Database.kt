package com.example.common

import com.example.common.tables.People
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import com.zaxxer.hikari.util.IsolationLevel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.DatabaseConfig
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Transaction
import java.util.concurrent.Executors

object Database {
    lateinit var database: Database
    lateinit var dispatcher: CoroutineDispatcher

    val poolSize = System.getenv("DB_POOL_SIZE")?.toInt() ?: 3
    val coroutinesPerPool = poolSize * (
        System.getenv("DB_COROUTINES_PER_POOL")?.toInt() ?: 4
    )

    val transactionSemaphore = Semaphore(coroutinesPerPool)

    suspend fun init(
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
            maximumPoolSize = poolSize
            transactionIsolation = IsolationLevel.TRANSACTION_REPEATABLE_READ.name
            addDataSourceProperty("reWriteBatchedInserts", "true")
            isAutoCommit = false
        }

        dispatcher = Executors.newCachedThreadPool().asCoroutineDispatcher()

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

    suspend fun <T> transaction(repetitions: Int = 5, transactionIsolation: Int? = null, statement: suspend Transaction.() -> T) = net.perfectdreams.exposedpowerutils.sql.transaction(
        dispatcher,
        database,
        repetitions,
        transactionIsolation,
        {
            transactionSemaphore.withPermit {
                it.invoke()
            }
        },
        statement
    )
}