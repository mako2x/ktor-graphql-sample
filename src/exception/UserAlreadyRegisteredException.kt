package com.example.exception

class UserAlreadyRegisteredException(username: String) : RuntimeException("'$username' has already been registered")

