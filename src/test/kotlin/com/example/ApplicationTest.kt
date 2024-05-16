package com.example

import com.example.plugins.Data
import com.example.plugins.DataRepository
import com.example.plugins.dataController
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    var fakeData = mutableListOf<Data>()

    private val testApp = TestApplication {
        install(Koin) {
            slf4jLogger()
            modules(
                module {
                    single<DataRepository> {
                        object : DataRepository {
                            override fun findAll() = fakeData
                            override fun save(data: Data) = fakeData.add(data)
                            override fun find(id: Int) = fakeData.find { it.id == id }
                            override fun delete(data: Data) = fakeData.remove(data)
                        }
                    }
                }
            )
        }

        application {
            dataController()
        }
    }

    private val client = testApp.createClient {
        install(ContentNegotiation) {
            json()
        }
    }

    @BeforeTest
    fun initializeState(){
        fakeData = mutableListOf(
            Data(1, "aaa"),
            Data(2, "bbb"),
            Data(3, "ccc"),
        )
    }

    @Test
    fun `test root endpoint`(): Unit = runBlocking {
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, World!", bodyAsText())
        }
    }

    @Test
    fun `get all data`(): Unit = runBlocking {
        client.get("/data").apply {
            assertEquals(HttpStatusCode.OK, status)
            val data = Json.decodeFromString<List<Data>>(bodyAsText())
            assertEquals(fakeData.size, data.size)
        }
    }

    @Test
    fun `post data instance`(): Unit = runBlocking {
        val response = client.post("/data") {
            contentType(ContentType.Application.Json)
            setBody(Data(3, "test"))
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Data added successfully", response.bodyAsText())
    }

    @Test
    fun `put data instance`(): Unit = runBlocking {
        val updatedDataResponse = client.put("/data") {
            contentType(ContentType.Application.Json)
            setBody(Data(3, "test2"))
        }
        assertEquals(HttpStatusCode.OK, updatedDataResponse.status)
        assertEquals("Data updated successfully", updatedDataResponse.bodyAsText())
    }

    @Test
    fun `delete data instance`(): Unit = runBlocking {
        client.delete("/data/1").apply {
            // Assertions to confirm the successful deletion of the Data instance
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Data deleted successfully", bodyAsText())
        }

        client.get("/data/1").apply {
            // Assertions to confirm the successful fetching of the updated Data instances
            assertEquals(HttpStatusCode.NotFound, status)
        }
    }

}
