package me.diamondforge.mee6

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.diamondforge.mee6.models.Mee6Response
import me.diamondforge.mee6.models.Mee6User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class Mee6XpClientAdditionalTest {
    private val mockEngine =
        MockEngine { request ->
            val url = request.url.toString()

            if (url.contains("leaderboard/123") && url.contains("page=0")) {
                val players = mutableListOf<Mee6User>()
                
                for (i in 1..1000) {
                    players.add(
                        Mee6User(
                            id = 1000L + i,
                            username = "FillerUser$i",
                            discriminator = "0000",
                            level = 1,
                            detailedXp = listOf(10L, 20L, 100L),
                        )
                    )
                }
                
                val response = Mee6Response(players = players)
                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/123") && url.contains("page=1")) {
                val response =
                    Mee6Response(
                        players =
                            listOf(
                                Mee6User(
                                    id = 789L,
                                    username = "PageTwoUser",
                                    discriminator = "9012",
                                    level = 2,
                                    detailedXp = listOf(25L, 50L, 250L),
                                ),
                            ),
                    )

                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/234") && url.contains("page=0")) {
                val response =
                    Mee6Response(
                        players =
                            listOf(
                                Mee6User(
                                    id = 123L,
                                    username = "ShortXpUser",
                                    discriminator = "1234",
                                    level = 1,
                                    detailedXp = listOf(10L),
                                ),
                                Mee6User(
                                    id = 456L,
                                    username = "EmptyXpUser",
                                    discriminator = "5678",
                                    level = 0,
                                    detailedXp = emptyList(),
                                ),
                            ),
                    )

                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else {
                respond(
                    content = "{}",
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            }
        }

    private val mockClient =
        HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                    },
                )
            }
        }

    private val client = Mee6XpClient(mockClient)

    @Test
    fun `getUserXp finds user on second page`() =
        runBlocking {
            val result = client.getUserXp(123L, 789L)

            assertEquals(789L, result?.id)
            assertEquals("PageTwoUser", result?.username)
            assertEquals("9012", result?.discriminator)
            assertEquals(2, result?.level)
            assertEquals(250L, result?.totalXp)
            assertEquals(1001, result?.rank)
        }

    @Test
    fun `getUserXp handles incomplete detailedXp list`() =
        runBlocking {
            val result = client.getUserXp(234L, 123L)

            assertEquals(123L, result?.id)
            assertEquals("ShortXpUser", result?.username)
            assertEquals("1234", result?.discriminator)
            assertEquals(1, result?.level)
            assertEquals(0L, result?.totalXp)
            assertEquals(1, result?.rank)
        }

    @Test
    fun `getUserXp handles empty detailedXp list`() =
        runBlocking {
            val result = client.getUserXp(234L, 456L)

            assertEquals(456L, result?.id)
            assertEquals("EmptyXpUser", result?.username)
            assertEquals("5678", result?.discriminator)
            assertEquals(0, result?.level)
            assertEquals(0L, result?.totalXp)
            assertEquals(2, result?.rank)
        }

    @Test
    fun `getAllUsers handles incomplete detailedXp lists`() =
        runBlocking {
            val result = client.getAllUsers(234L)

            assertEquals(2, result.size)
            
            assertEquals(123L, result[0].id)
            assertEquals("ShortXpUser", result[0].username)
            assertEquals(0L, result[0].totalXp)
            
            assertEquals(456L, result[1].id)
            assertEquals("EmptyXpUser", result[1].username)
            assertEquals(0L, result[1].totalXp)
        }

    @Test
    fun `close method can be called without exceptions`() {
        val testClient = Mee6XpClient()
        testClient.close()
    }
}