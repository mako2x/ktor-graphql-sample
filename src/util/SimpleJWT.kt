package com.example.util

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm

class SimpleJWT(val secret: String) {
    private val algorithm = Algorithm.HMAC256(secret)
    val verifier = JWT.require(algorithm).build()
    fun sign(name: String): String = JWT.create().withClaim("name", name).sign(algorithm)
}

