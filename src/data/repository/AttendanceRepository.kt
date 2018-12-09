package com.example.data.repository

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
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

    fun findByIds(ids: List<Int>): List<AttendanceEntity> {
        return transaction {
            AttendanceEntity.forIds(ids).toList()
        }
    }

    fun findByUserId(userId: Int): List<AttendanceEntity> {
        return transaction {
            AttendanceEntity.find { Attendances.userId.eq(userId) }.toList()
        }
    }

    fun save(userId: Int, date: DateTime, status: AttendanceStatus): AttendanceEntity {
        return transaction {
            AttendanceEntity.new {
                this.userId = userId
                this.date = date
                this.status = status
            }
        }
    }
}