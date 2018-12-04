package com.example

import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.UserRepository
import com.example.data.table.Attendances
import com.example.data.table.Users
import com.example.di.appModule
import com.example.exception.InvalidCredentialsException
import com.example.graphql.AppSchema
import com.example.graphql.GraphQLRequest
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
import io.ktor.features.StatusPages
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.get
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
import org.joda.time.DateTime
import org.koin.ktor.ext.inject
import org.koin.standalone.StandAloneContext.startKoin

fun main(args: Array<String>) {
    startKoin(listOf(appModule))
    Database.connect("jdbc:mysql://localhost/ktor_sample", "com.mysql.jdbc.Driver", "test", "test")
    transaction {
        addLogger(StdOutSqlLogger)
        SchemaUtils.create (Users, Attendances)
        val user = UserEntity.new {
            username = "kiyoko.fukuhara"
            password = "secret"
        }
        AttendanceEntity.new {
            this.user = user
            this.date = DateTime.now()
            this.status = 1
        }
        println("Users: ${UserEntity.all().joinToString {it.username}}")
        println("Attendances: ${AttendanceEntity.all().joinToString {it.date.toString()}}")
    }
    embeddedServer(Netty, commandLineEnvironment(args)).start()
}

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.main(testing: Boolean = false) {
    val userRepository: UserRepository by inject()

    val appSchema: AppSchema by inject()

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

    val simpleJWT = SimpleJWT("my-super-secret-for-jwt")
    install(Authentication) {
        jwt {
            verifier(simpleJWT.verifier)
            validate {
                UserIdPrincipal(it.payload.getClaim("name").asString())
            }
        }
    }

    install(ContentNegotiation) {
        jackson {
            enable(SerializationFeature.INDENT_OUTPUT)
        }
    }

    install(StatusPages) {
        exception<InvalidCredentialsException> { exception ->
            call.respond(
                HttpStatusCode.Unauthorized, mapOf(
                    "OK" to false,
                    "error" to (exception.message ?: "")
                )
            )
        }
    }

    routing {
        get("/") {
            call.respondText("HELLO WORLD!", contentType = ContentType.Text.Plain)
        }

        post("/login-register") {
            val post = call.receive<LoginRegister>()
            val user = userRepository.findByUsername(post.username) ?: userRepository.save(post.username, post.password)
            if (user.password != post.password) throw InvalidCredentialsException("Invalid credentials")
            call.respond(mapOf("token" to simpleJWT.sign(user.username)))
        }

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
            call.respondText(appSchema.schema.execute(request.query), contentType = ContentType.Application.Json)
        }
    }
}

class LoginRegister(val username: String, val password: String)
