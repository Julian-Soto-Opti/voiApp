package com.jetbrains.kmpapp.di

import com.jetbrains.kmpapp.data.AviationStorage
import com.jetbrains.kmpapp.data.InMemoryAviationStorage
import com.jetbrains.kmpapp.viewmodels.*
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val dataModule = module {
    single {
        val json = Json { ignoreUnknownKeys = true }
        HttpClient {
            install(ContentNegotiation) {
                json(json, contentType = ContentType.Any)
            }
        }
    }

    single<AviationStorage> { InMemoryAviationStorage() }
}

val viewModelModule = module {
    factoryOf(::ThemeViewModel)
    factoryOf(::LoginViewModel)
    factoryOf(::FollowUpViewModel)
    factoryOf(::ExecutiveViewModel)
    factoryOf(::CCOViewModel)
    factoryOf(::GanttViewModel)
}

fun initKoin() {
    startKoin {
        modules(
            dataModule,
            viewModelModule,
        )
    }
}
