package com.example.data.entity

import com.example.data.table.Attendances
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass

class AttendanceEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<AttendanceEntity>(Attendances)

    var userId by Attendances.userId
    var date by Attendances.date
    var status by Attendances.status
}
