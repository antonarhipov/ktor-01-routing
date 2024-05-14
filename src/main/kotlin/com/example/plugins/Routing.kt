package com.example.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Data(val value: Int, val text: String)

var dataList = mutableListOf<Data>()

fun Application.configureRouting() {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            this.isLenient = true
        })
    }

    routing {
        get("/") {
            call.respondText("Hello, World!")
        }

        get("/data") {
            call.respond<MutableList<Data>>(dataList)
        }

        post("/data") {
            val data = call.receive<Data>()
            dataList.add(data)
            call.respond<String>(status = HttpStatusCode.Created, message = "Data added successfully")
        }

        get("/data/{id}") {
            val id = call.parameters["id"]?.toInt()
            id?.let {
                val data = dataList.find { it.value == id }
                if (data != null) {
                    call.respond<Data>(data)
                } else {
                    call.respond<String>(status = HttpStatusCode.NotFound, message = "Data not found")
                }
            }
        }

        put("/data") {
            val newData = call.receive<Data>()
            val oldData = dataList.find { it.value == newData.value }
            if (oldData != null) {
                dataList.remove(oldData)
                dataList.add(newData)
                call.respond<String>(status = HttpStatusCode.OK, message = "Data updated successfully")
            } else {
                call.respond<String>(status = HttpStatusCode.NotFound, message = "Data to update not found")
            }
        }

        delete("/data/{id}") {
            val id = call.parameters["id"]?.toInt()
            id?.let {
                val data = dataList.find { it.value == id }
                if (data != null) {
                    dataList.remove(data)
                    call.respond<String>(status = HttpStatusCode.OK, message = "Data deleted successfully")
                } else {
                    call.respond<String>(status = HttpStatusCode.NotFound, message = "Data to delete not found")
                }
            }
        }
    }
}

