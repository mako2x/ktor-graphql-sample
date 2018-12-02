package com.example.data.datasource

import com.example.data.entity.UserEntity
import java.util.*

private val users = Collections.synchronizedList(
    mutableListOf(
        UserEntity(1, "takashi.kihara", "password"),
        UserEntity(2, "chikako.yumeishi", "password")
    )
)

class UserDataSource {

    fun findAll(): List<UserEntity> = users

    fun findById(id: Int) = users.firstOrNull { it.id == id }

    fun findByUsername(username: String) = users.firstOrNull { it.username == username }

    fun save(username: String, password: String): UserEntity {
        val user = UserEntity(users.size + 1, username, password)
        users += user
        return user
    }
}