package com.example.graphql.type

import com.example.data.entity.UserEntity

data class User(
    val id: Int,
    val username: String
) {

    companion object {
        fun fromEntity(entity: UserEntity) = User(entity.id.value, entity.username)
    }
}


