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
import org.koin.dsl.module
import org.koin.ktor.ext.inject

@Serializable
data class Data(val id: Int, val text: String)

val dataModule = module {
    single<DataRepository> { DataRepositoryImpl }
}

interface DataRepository {
    fun findAll(): MutableList<Data>
    fun save(data: Data): Boolean
    fun find(id: Int): Data?
    fun delete(data: Data): Boolean
}

object DataRepositoryImpl : DataRepository {
    private var storage = mutableListOf<Data>()
    override fun findAll() = storage
    override fun save(data: Data) = storage.add(data)
    override fun find(id: Int) = storage.find { it.id == id }
    override fun delete(data: Data) = storage.remove(data)
}

fun Application.dataController() {

    val repository by inject<DataRepository>()

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
            call.respond<MutableList<Data>>(repository.findAll())
        }

        post("/data") {
            val data = call.receive<Data>()
            repository.save(data)
            call.respond<String>(status = HttpStatusCode.Created, message = "Data added successfully")
        }

        get("/data/{id}") {
            val id = call.parameters["id"]?.toInt()
            id?.let {
                val data = repository.find(id)
                if (data != null) {
                    call.respond<Data>(data)
                } else {
                    call.respond<String>(status = HttpStatusCode.NotFound, message = "Data not found")
                }
            }
        }

        put("/data") {
            val newData = call.receive<Data>()
            val oldData = repository.find(newData.id)
            if (oldData != null) {
                repository.delete(oldData)
                repository.save(newData)
                call.respond<String>(status = HttpStatusCode.OK, message = "Data updated successfully")
            } else {
                call.respond<String>(status = HttpStatusCode.NotFound, message = "Data to update not found")
            }
        }

        delete("/data/{id}") {
            val id = call.parameters["id"]?.toInt()
            id?.let {
                val data = repository.find(id)
                if (data != null) {
                    repository.delete(data)
                    call.respond<String>(status = HttpStatusCode.OK, message = "Data deleted successfully")
                } else {
                    call.respond<String>(status = HttpStatusCode.NotFound, message = "Data to delete not found")
                }
            }
        }
    }
}
