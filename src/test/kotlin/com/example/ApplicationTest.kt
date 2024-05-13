package com.example

import com.example.plugins.Data
import com.example.plugins.configureRouting
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun testRootEndpoint() = testApplication {
        application {
            configureRouting()
        }
        client.get("/").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello, World!", bodyAsText())
        }
    }

    @Test
    fun testCRUDFunctionality() = testApplication {
        //region Application & Client
        application {
            configureRouting()
        }
        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }
        //endregion

        //region POST
        val response = client.post("/data") {
            contentType(ContentType.Application.Json)
            setBody(Data(3, "test"))
        }
        // Assertions to confirm the successful creation of the Data instance
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Data added successfully", response.bodyAsText())

        // Test GET request to retrieve the list of Data instances
        client.get("/data").apply {
            // Assertions to confirm the successful fetching of the Data instances
            assertEquals(HttpStatusCode.OK, status)
            val data = Json.decodeFromString<List<Data>>(bodyAsText())
            assertEquals(1, data.size)
            assertEquals(3, data[0].value)
            assertEquals("test", data[0].text)
        }
        //endregion

        //region PUT
        val updatedDataResponse = client.put("/data") {
            contentType(ContentType.Application.Json)
            setBody(Data(3, "test2"))
        }
        // Assertions to confirm the successful update of the Data instance
        assertEquals(HttpStatusCode.OK, updatedDataResponse.status)
        assertEquals("Data updated successfully", updatedDataResponse.bodyAsText())

        // Test GET request to confirm the update
        client.get("/data").apply {
            // Assertions to confirm the successful fetching of the updated Data instances
            assertEquals(HttpStatusCode.OK, status)
            val data = Json.decodeFromString<List<Data>>(bodyAsText())
            println(data)
            assertEquals(1, data.size)
            assertEquals(3, data[0].value)
            assertEquals("test2", data[0].text)
        }
        //endregion

        //region DELETE
        client.delete("/data/3").apply {
            // Assertions to confirm the successful deletion of the Data instance
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Data deleted successfully", bodyAsText())
        }
        //endregion

        //region 404
        client.get("/data/3").apply {
            // Assertions to confirm the successful fetching of the updated Data instances
            assertEquals(HttpStatusCode.NotFound, status)
        }
        //endregion
    }

}
