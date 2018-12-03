package com.example.data.table

import org.jetbrains.exposed.dao.IntIdTable

object Attendances : IntIdTable() {
    val user = reference("user_id", Users)
    val date = date("date")
    val status = integer("status")
}