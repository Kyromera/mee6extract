package me.diamondforge.mee6

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import me.diamondforge.mee6.exceptions.GuildNotFoundException
import me.diamondforge.mee6.exceptions.LeaderboardNotPublicException
import me.diamondforge.mee6.models.Mee6Error
import me.diamondforge.mee6.models.Mee6ErrorResponse
import me.diamondforge.mee6.models.Mee6Response
import me.diamondforge.mee6.models.Mee6User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class Mee6XpClientTest {
    private val mockEngine =
        MockEngine { request ->
            val url = request.url.toString()

            if (url.contains("leaderboard/123") && url.contains("page=0")) {
                val response =
                    Mee6Response(
                        players =
                            listOf(
                                Mee6User(
                                    id = 123L,
                                    username = "TestUser",
                                    discriminator = "1234",
                                    level = 5,
                                    detailedXp = listOf(100L, 200L, 1000L),
                                ),
                                Mee6User(
                                    id = 456L,
                                    username = "OtherUser",
                                    discriminator = "5678",
                                    level = 3,
                                    detailedXp = listOf(50L, 100L, 500L),
                                ),
                            ),
                    )

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
            } else if (url.contains("leaderboard/345") && url.contains("page=0")) {
                val response =
                    Mee6Response(
                        players =
                            List(1000) { index ->
                                Mee6User(
                                    id = 1000L + index,
                                    username = "User$index",
                                    discriminator = "0000",
                                    level = 1,
                                    detailedXp = listOf(10L, 20L, 100L),
                                )
                            },
                    )

                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/345") && url.contains("page=1")) {
                val response =
                    Mee6Response(
                        players =
                            List(500) { index ->
                                Mee6User(
                                    id = 2000L + index,
                                    username = "User${1000 + index}",
                                    discriminator = "0000",
                                    level = 1,
                                    detailedXp = listOf(10L, 20L, 100L),
                                )
                            },
                    )

                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/456") && url.contains("page=0")) {
                val response = Mee6Response(players = emptyList())
                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/567") && url.contains("page=0")) {
                val players = mutableListOf<Mee6User>()

                for (i in 1..1000) {
                    players.add(
                        Mee6User(
                            id = 1000L + i,
                            username = "FillerUser$i",
                            discriminator = "0000",
                            level = 1,
                            detailedXp = listOf(10L, 20L, 100L),
                        ),
                    )
                }

                val response = Mee6Response(players = players)
                val jsonContent = Json.encodeToString(Mee6Response.serializer(), response)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/567") && url.contains("page=1")) {
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
            } else if (url.contains("leaderboard/789")) {
                respond(
                    content = "Error",
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "text/plain"),
                )
            } else if (url.contains("leaderboard/404")) {
                val errorResponse =
                    Mee6ErrorResponse(
                        statusCode = 404,
                        error = Mee6Error(message = "Guild not found"),
                    )
                val jsonContent = Json.encodeToString(Mee6ErrorResponse.serializer(), errorResponse)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.NotFound,
                    headers = headersOf(HttpHeaders.ContentType, "application/json"),
                )
            } else if (url.contains("leaderboard/401")) {
                val errorResponse =
                    Mee6ErrorResponse(
                        statusCode = 401,
                        error =
                            Mee6Error(
                                message = "The server could not verify that you are authorized to access the URL requested. You either supplied the wrong credentials (e.g. a bad password), or your browser doesn't understand how to supply the credentials required.",
                            ),
                    )
                val jsonContent = Json.encodeToString(Mee6ErrorResponse.serializer(), errorResponse)
                respond(
                    content = jsonContent,
                    status = HttpStatusCode.Unauthorized,
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
    fun `getUserXp returns correct user data when user exists`() =
        runBlocking {
            val result = client.getUserXp(123L, 123L)

            assertEquals(123L, result?.id)
            assertEquals("TestUser", result?.username)
            assertEquals("1234", result?.discriminator)
            assertEquals(5, result?.level)
            assertEquals(1000L, result?.totalXp)
            assertEquals(1, result?.rank)
        }

    @Test
    fun `getUserXp returns null when user does not exist`() =
        runBlocking {
            val result = client.getUserXp(123L, 999L)

            assertNull(result)
        }

    @Test
    fun `getUserXp returns null when guild has no players`() =
        runBlocking {
            val result = client.getUserXp(456L, 123L)

            assertNull(result)
        }

    @Test
    fun `getUserXp returns null when API returns error`() =
        runBlocking {
            val result = client.getUserXp(789L, 123L)

            assertNull(result)
        }

    @Test
    fun `getAllUsers returns list of users when guild has players`() =
        runBlocking {
            val result = client.getAllUsers(123L)

            assertEquals(2, result.size)

            assertEquals(123L, result[0].id)
            assertEquals("TestUser", result[0].username)
            assertEquals("1234", result[0].discriminator)
            assertEquals(5, result[0].level)
            assertEquals(1000L, result[0].totalXp)
            assertEquals(1, result[0].rank)

            assertEquals(456L, result[1].id)
            assertEquals("OtherUser", result[1].username)
            assertEquals("5678", result[1].discriminator)
            assertEquals(3, result[1].level)
            assertEquals(500L, result[1].totalXp)
            assertEquals(2, result[1].rank)
        }

    @Test
    fun `getAllUsers returns empty list when guild has no players`() =
        runBlocking {
            val result = client.getAllUsers(456L)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getAllUsers returns empty list when API returns error`() =
        runBlocking {
            val result = client.getAllUsers(789L)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getAllUsers also returns correct rank information when guild has players`() =
        runBlocking {
            val result = client.getAllUsers(123L)

            assertEquals(2, result.size)

            assertEquals(123L, result[0].id)
            assertEquals("TestUser", result[0].username)
            assertEquals("1234", result[0].discriminator)
            assertEquals(5, result[0].level)
            assertEquals(1000L, result[0].totalXp)
            assertEquals(1, result[0].rank)

            assertEquals(456L, result[1].id)
            assertEquals("OtherUser", result[1].username)
            assertEquals("5678", result[1].discriminator)
            assertEquals(3, result[1].level)
            assertEquals(500L, result[1].totalXp)
            assertEquals(2, result[1].rank)
        }

    @Test
    fun `getAllUsers returns empty list when guild has no players (second test)`() =
        runBlocking {
            val result = client.getAllUsers(456L)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getAllUsers returns empty list when API returns error (second test)`() =
        runBlocking {
            val result = client.getAllUsers(789L)

            assertTrue(result.isEmpty())
        }

    @Test
    fun `getUserXp finds user on second page`() =
        runBlocking {
            val result = client.getUserXp(345L, 2000L)

            assertEquals(2000L, result?.id)
            assertEquals("User1000", result?.username)
            assertEquals("0000", result?.discriminator)
            assertEquals(1, result?.level)
            assertEquals(100L, result?.totalXp)
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
    fun `getAllUsers handles pagination correctly`() =
        runBlocking {
            val result = client.getAllUsers(345L)

            assertEquals(1500, result.size)

            assertEquals(1000L, result[0].id)
            assertEquals("User0", result[0].username)
            assertEquals(1, result[0].rank)

            assertEquals(2000L, result[1000].id)
            assertEquals("User1000", result[1000].username)
            assertEquals(1001, result[1000].rank)

            assertEquals(2499L, result[1499].id)
            assertEquals("User1499", result[1499].username)
            assertEquals(1500, result[1499].rank)
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

    @Test
    fun `getUserXp throws GuildNotFoundException when guild does not exist`() {
        assertThrows<GuildNotFoundException> {
            runBlocking {
                client.getUserXp(404L, 123L)
            }
        }.also { exception ->
            assertTrue(exception.message?.contains("Guild not found") ?: false)
            assertTrue(exception.message?.contains("404") ?: false)
        }
    }

    @Test
    fun `getAllUsers throws GuildNotFoundException when guild does not exist`() {
        assertThrows<GuildNotFoundException> {
            runBlocking {
                client.getAllUsers(404L)
            }
        }.also { exception ->
            assertTrue(exception.message?.contains("Guild not found") ?: false)
            assertTrue(exception.message?.contains("404") ?: false)
        }
    }

    @Test
    fun `getUserXp throws LeaderboardNotPublicException when leaderboard is not public`() {
        assertThrows<LeaderboardNotPublicException> {
            runBlocking {
                client.getUserXp(401L, 123L)
            }
        }.also { exception ->
            assertTrue(exception.message?.contains("not public") ?: false)
            assertTrue(exception.message?.contains("401") ?: false)
        }
    }

    @Test
    fun `getAllUsers throws LeaderboardNotPublicException when leaderboard is not public`() {
        assertThrows<LeaderboardNotPublicException> {
            runBlocking {
                client.getAllUsers(401L)
            }
        }.also { exception ->
            assertTrue(exception.message?.contains("not public") ?: false)
            assertTrue(exception.message?.contains("401") ?: false)
        }
    }
}
