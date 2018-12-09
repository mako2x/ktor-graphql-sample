package com.example

import com.example.data.table.Attendances
import com.example.data.table.Users
import com.example.di.appModule
import com.example.graphql.AppSchema
import com.example.graphql.GraphQLRequest
import com.example.graphql.toSpecificationJson
import com.example.util.SimpleJWT
import com.fasterxml.jackson.databind.SerializationFeature
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.UserIdPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CORS
import io.ktor.features.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.commandLineEnvironment
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext.startKoin

fun main(args: Array<String>) {
    startKoin(listOf(appModule))
    Database.connect(
        "jdbc:mysql://localhost/ktor_sample?useSSL=false",
        "com.mysql.jdbc.Driver",
        "test",
        "test"
    )
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users, Attendances)
    }
    embeddedServer(Netty, commandLineEnvironment(args)).start()
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {
    val appSchema: AppSchema by inject()

    val simpleJWT: SimpleJWT by inject()

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)
        header(HttpHeaders.Authorization)
        header("MyCustomHeader")
        allowCredentials = true
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }

    install(Authentication) {
        jwt {
            verifier(simpleJWT.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("id").asString())
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    routing {
        //        route("/snippets") {
//            authenticate {
//                post {
//                    val post = call.receive<PostSnippet>()
//                    val principal = call.principal<UserIdPrincipal>() ?: error("No principal")
//                    snippets += Snippet(principal.name, post.snippet.text)
//                    call.respond(mapOf("OK" to true))
//                }
//            }
//        }

        post("/graphql") {
            val request = call.receive<GraphQLRequest>()
            val executionResult = appSchema.execute(request.query)
            call.respondText(executionResult.toSpecificationJson(), contentType = ContentType.Application.Json)
        }
    }
}
