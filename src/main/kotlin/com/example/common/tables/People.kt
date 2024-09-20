package com.example.common.tables

import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.date

object People : Table("people") {
    val id = integer("id").autoIncrement()
    val name = varchar("name", length = 256)
    val nickname = varchar("nickname", length = 256).uniqueIndex()
    val birthdate = date("birthdate")
    val stacks = varchar("stack", length = 512)

    override val primaryKey = PrimaryKey(id)
}