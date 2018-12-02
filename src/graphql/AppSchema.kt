package com.example.graphql

import com.example.constant.AttendanceStatus
import com.example.data.datasource.AttendanceDataSource
import com.example.data.datasource.UserDataSource
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import com.github.pgutkowski.kgraphql.KGraphQL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AppSchema(
    private val userDataSource: UserDataSource,
    private val attendanceDataSource: AttendanceDataSource
) {

    val schema = KGraphQL.schema {
        query("users") {
            resolver { -> userDataSource.findAll() }
        }

        query("user") {
            resolver { id: Int -> userDataSource.findById(id) }
        }

        query("attendances") {
            resolver { -> attendanceDataSource.findAll() }
        }

        query("attendance") {
            resolver { id: Int -> attendanceDataSource.findById(id) }
        }

        mutation("createAttendance") {
            description = "Creates attendance"
            resolver { userId: Int, date: LocalDate, status: AttendanceStatus ->
                attendanceDataSource.save(userId, date, status)
            }
        }

        type<UserEntity> {
            UserEntity::password.ignore()
            property<List<AttendanceEntity>>("attendances") {
                resolver { attendanceDataSource.findByUserId(it.id) }
            }
        }

        type<AttendanceEntity> {
            property<UserEntity>("user") {
                resolver { userDataSource.findById(it.userId)!! }
            }
        }

        enum<AttendanceStatus>()

        stringScalar<LocalDate> {
            deserialize = { LocalDate.parse(it, DateTimeFormatter.ISO_DATE) }
            serialize = { it.format(DateTimeFormatter.ISO_DATE) }
        }
    }
}
