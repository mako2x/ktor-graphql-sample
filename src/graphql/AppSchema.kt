package com.example.graphql

import com.example.constant.AttendanceStatus
import com.example.data.entity.AttendanceEntity
import com.example.data.entity.UserEntity
import com.example.data.repository.AttendanceRepository
import com.example.data.repository.UserRepository
import com.example.graphql.type.Attendance
import com.example.graphql.type.User
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ExecutionInput.newExecutionInput
import graphql.ExecutionResult
import graphql.GraphQL
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentation
import graphql.execution.instrumentation.dataloader.DataLoaderDispatcherInstrumentationOptions
import graphql.schema.*
import graphql.schema.idl.RuntimeWiring.newRuntimeWiring
import graphql.schema.idl.SchemaGenerator
import graphql.schema.idl.SchemaParser
import graphql.schema.idl.TypeRuntimeWiring.newTypeWiring
import org.dataloader.BatchLoader
import org.dataloader.DataLoader
import org.dataloader.DataLoaderRegistry
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.InputStreamReader
import java.io.Reader
import java.util.concurrent.CompletableFuture

class AppSchema(
    private val userRepository: UserRepository,
    private val attendanceRepository: AttendanceRepository
) {
    private val graphql: GraphQL

    private val dataLoaderRegistry = buildDataLoaderRegistry()

    private val dateFormat = DateTimeFormat.forPattern("yyyy-MM-dd")

    init {
        val typeDefinitionRegistry = SchemaParser().parse(loadSchemaFile())
        val runtimeWiring = buildRuntimeWiring()
        val graphQLSchema = SchemaGenerator().makeExecutableSchema(typeDefinitionRegistry, runtimeWiring)
        graphql = GraphQL.newGraphQL(graphQLSchema)
            .instrumentation(
                DataLoaderDispatcherInstrumentation(
                    DataLoaderDispatcherInstrumentationOptions.newOptions()
                )
            )
            .build()
    }

    private fun loadSchemaFile(): Reader {
        val stream = AppSchema::class.java.getResourceAsStream("/sample.graphqls")
        return InputStreamReader(stream)
    }

    private fun buildRuntimeWiring() = newRuntimeWiring()
        .type(newTypeWiring("Query")
            .dataFetcher("users") {
                userRepository.findAll().map { user -> User.fromEntity(user) }
            }
            .dataFetcher("user") { environment ->
                val userId = environment.getArgument<Int>("id")
                val userDataLoader = environment.getDataLoader<Int, UserEntity>("user")
                userDataLoader.load(userId)
                    .thenApply { User.fromEntity(it) }
            }
            .dataFetcher("attendances") {
                attendanceRepository.findAll().map { attendance -> Attendance.fromEntity(attendance) }
            }
            .dataFetcher("attendance") { environment ->
                val attendanceId = environment.getArgument<Int>("id")
                val attendanceDataLoader = environment.getDataLoader<Int, AttendanceEntity>("attendance")
                attendanceDataLoader.load(attendanceId)
                    .thenApply { Attendance.fromEntity(it) }
            }
        )
        .type(newTypeWiring("Mutation")
            .dataFetcher("addAttendance") { environment ->
                val userId = environment.arguments["userId"] as? Int ?: return@dataFetcher null
                val dateText = environment.arguments["date"] as? String ?: return@dataFetcher null
                val statusText = environment.arguments["status"] as? String ?: return@dataFetcher null
                Attendance.fromEntity(
                    attendanceRepository.save(
                        userId,
                        dateFormat.parseDateTime(dateText),
                        AttendanceStatus.valueOf(statusText)
                    )
                )
            }
        )
        .type(newTypeWiring("User")
            .dataFetcher("attendances") { environment ->
                val user = environment.getSource<User>()
                attendanceRepository.findByUserId(user.id).map { Attendance.fromEntity(it) }
            }
        )
        .type(newTypeWiring("Attendance")
            .dataFetcher("user") { environment ->
                val attendance = environment.getSource<Attendance>()
                val userDataLoader = environment.getDataLoader<Int, UserEntity>("user")
                userDataLoader.load(attendance.userId)
                    .thenApply { User.fromEntity(it) }
            }
        )
        .scalar(GraphQLScalarType("Date", "Date", object : Coercing<DateTime, String> {
            override fun serialize(dataFetcherResult: Any?): String {
                if (dataFetcherResult is DateTime) {
                    return dateFormat.print(dataFetcherResult)
                }
                throw CoercingSerializeException("Unable to serialize $dataFetcherResult")
            }

            override fun parseValue(input: Any?): DateTime {
                if (input is String) {
                    return dateFormat.parseDateTime(input)
                }
                throw CoercingParseValueException("Unable to parse $input")
            }

            override fun parseLiteral(input: Any?): DateTime {
                if (input is String) {
                    return dateFormat.parseDateTime(input)
                }
                throw CoercingParseLiteralException("Unable to parse $input")
            }
        }))
        .build()

    private fun buildDataLoaderRegistry() = DataLoaderRegistry()
        .register("user", DataLoader.newDataLoader(BatchLoader<Int, UserEntity> { ids ->
            CompletableFuture.supplyAsync { userRepository.findByIds(ids) }
        }))
        .register("attendance", DataLoader.newDataLoader(BatchLoader<Int, AttendanceEntity> { ids ->
            CompletableFuture.supplyAsync { attendanceRepository.findByIds(ids) }
        }))

    fun execute(query: String): ExecutionResult = graphql.execute(
        newExecutionInput()
            .query(query)
            .dataLoaderRegistry(dataLoaderRegistry)
            .build()
    )
}

private val jackson = ObjectMapper()
fun ExecutionResult.toSpecificationJson(): String = jackson.writeValueAsString(this.toSpecification())
