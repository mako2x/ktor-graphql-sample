package com.example.data.datasource

import com.example.data.entity.AttendanceEntity
import com.example.graphql.AttendanceStatus
import java.time.LocalDate
import java.util.*

private val attendances = Collections.synchronizedList(
    mutableListOf(
        AttendanceEntity(1, 1, LocalDate.now(), AttendanceStatus.LATE),
        AttendanceEntity(1, 1, LocalDate.now().minusDays(1), AttendanceStatus.WORK_FROM_HOME),
        AttendanceEntity(1, 2, LocalDate.now().minusDays(5), AttendanceStatus.DAY_OFF)
    )
)

class AttendanceDataSource {

    fun findAll() = attendances

    fun findById(id: Int) = attendances.firstOrNull { it.id == id } ?: attendances.first()

    fun findByUserId(userId: Int) = attendances.filter { it.userId == userId }
}