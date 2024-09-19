package com.example.common

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date
import org.jetbrains.exposed.sql.transactions.transaction

class Database {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )

    object People : Table("people") {
        val id = integer("id").autoIncrement()
        val name = varchar("name", length = 256)
        val nickname = varchar("nickname", length = 256).uniqueIndex()
        val birthdate = date("birthdate")
        val stacks = varchar("stack", length = 512)

        override val primaryKey = PrimaryKey(id)
    }

    init {
        transaction(database) {
            SchemaUtils.create(People)
        }
    }

}