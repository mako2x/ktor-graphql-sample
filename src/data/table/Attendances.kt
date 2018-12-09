package com.example.data.table

import com.example.constant.AttendanceStatus
import org.jetbrains.exposed.dao.IntIdTable

object Attendances : IntIdTable(name = "attendances") {
    val userId = integer("user_id").index()
    val date = date("date")
    val status = enumeration("status", AttendanceStatus::class)
}