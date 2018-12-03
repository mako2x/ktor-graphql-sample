package com.example.di

import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository
import com.example.graphql.AppSchema
import org.koin.dsl.module.module

val appModule = module {
    single { UserRepository() }
    single { AttendanceRepository() }
    single { AppSchema(get(), get()) }
}