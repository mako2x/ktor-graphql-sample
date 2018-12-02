package com.example.di

import com.example.data.datasource.AttendanceDataSource
import com.example.data.datasource.UserDataSource
import com.example.graphql.AppSchema
import org.koin.dsl.module.module

val appModule = module {
    single { UserDataSource() }
    single { AttendanceDataSource() }
    single { AppSchema(get(), get()) }
}