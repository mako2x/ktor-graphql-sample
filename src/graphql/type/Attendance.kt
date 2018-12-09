package com.example.graphql.type

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
import org.joda.time.DateTime

data class Attendance(
    val id: Int,
    val userId: Int,
    val date: DateTime,
    val status: AttendanceStatus
) {
    companion object {
        fun fromEntity(entity: AttendanceEntity) = Attendance(entity.id.value, entity.userId, entity.date, entity.status)
    }
}
