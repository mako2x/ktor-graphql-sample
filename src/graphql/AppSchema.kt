package com.example.graphql

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository
import com.github.pgutkowski.kgraphql.KGraphQL
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

data class User(
    val id: Int,
    val username: String
) {
    companion object {
        fun fromEntity(entity: UserEntity) = User(entity.id.value, entity.username)
    }
}

data class Attendance(
    val id: Int,
    val userId: Int,
    val date: DateTime,
    val status: AttendanceStatus
) {
    companion object {
        fun fromEntity(entity: AttendanceEntity) = transaction {
            Attendance(entity.id.value, entity.user.id.value, entity.date, AttendanceStatus.LATE)
        }
    }
}

class AppSchema(
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
) {

    val schema = KGraphQL.schema {
        query("users") {
            resolver { -> userRepository.findAll().map { User.fromEntity(it) } }
        }

        query("user") {
            resolver { id: Int -> userRepository.findById(id)?.let { User.fromEntity(it) } }
        }

        query("attendances") {
            resolver { -> attendanceRepository.findAll().map { Attendance.fromEntity(it) } }
        }

        query("attendance") {
            resolver { id: Int -> attendanceRepository.findById(id)?.let { Attendance.fromEntity(it) } }
        }

        mutation("createAttendance") {
            description = "Creates attendance"
            resolver { userId: Int, date: DateTime, status: AttendanceStatus ->
                val user = userRepository.findById(userId) ?: return@resolver null
                val entity = attendanceRepository.save(user, date, status)
                Attendance.fromEntity(entity)
            }
        }

        type<User> {
            property<List<Attendance>>("attendances") {
                resolver { user ->
                    attendanceRepository.findByUserId(user.id).map { Attendance.fromEntity(it) }
                }
            }
        }

        type<Attendance> {
            property<User>("user") {
                resolver { User.fromEntity(userRepository.findById(it.userId)!!) }
            }
        }

        enum<AttendanceStatus>()

        val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")
        stringScalar<DateTime> {
            deserialize = { dateFormat.parseDateTime(it) }
            serialize = { dateFormat.print(it) }
        }
    }
}
