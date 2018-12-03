package com.example.data.repository

import com.example.data.entity.UserEntity
import com.example.data.table.Users
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    fun findAll(): List<UserEntity> {
        return transaction {
            UserEntity.all().toList()
        }
    }

    fun findById(id: Int): UserEntity? {
        return transaction {
            UserEntity.findById(id)
        }
    }

    fun findByUsername(username: String): UserEntity? {
        return transaction {
            UserEntity.find { Users.username eq username }.firstOrNull()
        }
    }

    fun save(username: String, password: String): UserEntity {
        return transaction {
            UserEntity.new {
                this.username = username
                this.password = password
            }
        }
    }
}