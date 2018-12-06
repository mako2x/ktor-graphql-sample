package com.example.data.repository

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import com.example.data.table.Attendances
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

    fun findByUserId(userId: Int): List<AttendanceEntity> {
        return transaction {
            AttendanceEntity.find { Attendances.user.eq(userId) }.toList()
        }
    }

    fun save(user: UserEntity, date: DateTime, status: AttendanceStatus): AttendanceEntity {
        return transaction {
            AttendanceEntity.new {
                this.user = user
                this.date = date
                this.status = status.rawValue
            }
        }
    }
}