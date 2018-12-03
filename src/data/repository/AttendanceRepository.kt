package com.example.data.repository

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

class AttendanceRepository {

    fun findAll(): List<AttendanceEntity> {
        return transaction {
            AttendanceEntity.all().toList()
        }
    }

    fun findById(id: Int): AttendanceEntity? {
        return transaction {
            AttendanceEntity.findById(id)
        }
    }

    fun save(user: UserEntity, date: DateTime, status: AttendanceStatus): AttendanceEntity {
        return transaction {
            AttendanceEntity.new {
                this.user = user
                this.date = date
                this.status = 1
            }
        }
    }
}