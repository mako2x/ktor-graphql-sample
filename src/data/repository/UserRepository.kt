package com.example.data.repository

import com.example.data.entity.UserEntity
import com.example.data.table.Users
import org.jetbrains.exposed.sql.and
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

    fun findByIds(ids: List<Int>): List<UserEntity> {
        return transaction {
            UserEntity.forIds(ids).toList()
        }
    }

    fun findByUsername(username: String): UserEntity? {
        return transaction {
            UserEntity.find { Users.username eq username }.firstOrNull()
        }
    }

    fun findByUsernameAndPassword(username: String, password: String): UserEntity? {
        return transaction {
            UserEntity.find { (Users.username eq username) and (Users.password eq password) }.firstOrNull()
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