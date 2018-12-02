package com.example.graphql

class GraphQLRequest(val query: String = "",
                     val operationName: String? = null,
                     val variables: Map<String, Any>? = null)

