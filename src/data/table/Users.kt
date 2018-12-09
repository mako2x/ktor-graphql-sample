package com.example.data.table

import org.jetbrains.exposed.dao.IntIdTable

object Users : IntIdTable(name = "users") {
    val username = varchar("username", 128)
    val password = varchar("password", 128)
}