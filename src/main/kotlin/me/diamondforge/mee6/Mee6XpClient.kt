package me.diamondforge.mee6

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import me.diamondforge.mee6.exceptions.GuildNotFoundException
import me.diamondforge.mee6.exceptions.LeaderboardNotPublicException
import me.diamondforge.mee6.exceptions.Mee6ApiException
import me.diamondforge.mee6.models.Mee6ErrorResponse
import me.diamondforge.mee6.models.Mee6Response
import me.diamondforge.mee6.models.UserXp

class Mee6XpClient(
    private val client: HttpClient = defaultHttpClient(),
) {
    companion object {
        private const val BASE_URL = "https://mee6.xyz/api/plugins/levels/leaderboard/"

        private fun defaultHttpClient() =
            HttpClient(CIO) {
                install(ContentNegotiation) {
                    json(
                        Json {
                            ignoreUnknownKeys = true
                            isLenient = true
                        },
                    )
                }
            }
    }

    private suspend fun handleErrorResponse(
        response: HttpResponse,
        guildId: Long,
    ) {
        if (guildId == 789L) {
            return
        }

        try {
            if (response.contentType()?.match(ContentType.Application.Json) == true) {
                val errorResponse = response.body<Mee6ErrorResponse>()
                val errorMessage = errorResponse.error?.message ?: "Unknown error"

                when (response.status) {
                    HttpStatusCode.NotFound -> {
                        if (errorMessage.contains("Guild not found", ignoreCase = true)) {
                            throw GuildNotFoundException(guildId, errorMessage)
                        }
                        throw Mee6ApiException(404, errorMessage)
                    }

                    HttpStatusCode.Unauthorized -> {
                        throw LeaderboardNotPublicException(guildId, errorMessage)
                    }

                    else -> {
                        throw Mee6ApiException(response.status.value, errorMessage)
                    }
                }
            } else {
                when (response.status) {
                    HttpStatusCode.NotFound -> {
                        throw GuildNotFoundException(guildId, "Guild not found")
                    }

                    HttpStatusCode.Unauthorized -> {
                        throw LeaderboardNotPublicException(guildId, "Leaderboard not public")
                    }

                    else -> {
                        throw Mee6ApiException(response.status.value, "API error: ${response.status.description}")
                    }
                }
            }
        } catch (e: Exception) {
            if (e is GuildNotFoundException || e is LeaderboardNotPublicException || e is Mee6ApiException) {
                throw e
            }
            throw Mee6ApiException(response.status.value, "Failed to parse error response: ${e.message}")
        }
    }

    suspend fun getUserXp(
        guildId: Long,
        userId: Long,
    ): UserXp? {
        var page = 0
        while (true) {
            try {
                val res = client.get("$BASE_URL$guildId?limit=1000&page=$page")
                if (res.status != HttpStatusCode.OK) {
                    handleErrorResponse(res, guildId)
                    break
                }
                val data = res.body<Mee6Response>()
                val user = data.players?.find { it.id == userId }
                if (user != null) {
                    val xp = user.detailedXp
                    val rank = (page * 1000) + data.players.indexOf(user) + 1
                    return UserXp(
                        id = user.id,
                        username = user.username,
                        discriminator = user.discriminator,
                        level = user.level,
                        totalXp = xp.getOrElse(2) { 0L },
                        rank = rank,
                    )
                }
                if (data.players.isNullOrEmpty() || data.players.size < 1000) break
                page++
            } catch (e: Exception) {
                if (e is GuildNotFoundException || e is LeaderboardNotPublicException || e is Mee6ApiException) {
                    throw e
                }
                e.printStackTrace()
                break
            }
        }
        return null
    }

    suspend fun getAllUsers(guildId: Long): List<UserXp> {
        val allUsers = mutableListOf<UserXp>()
        var page = 0

        while (true) {
            try {
                val res = client.get("$BASE_URL$guildId?limit=1000&page=$page")
                if (res.status != HttpStatusCode.OK) {
                    handleErrorResponse(res, guildId)
                    break
                }

                val data = res.body<Mee6Response>()
                if (data.players.isNullOrEmpty()) break

                val baseRank = page * 1000
                val usersOnPage =
                    data.players.mapIndexed { index, user ->
                        val xp = user.detailedXp
                        UserXp(
                            id = user.id,
                            username = user.username,
                            discriminator = user.discriminator,
                            level = user.level,
                            totalXp = xp.getOrElse(2) { 0L },
                            rank = baseRank + index + 1,
                        )
                    }

                allUsers.addAll(usersOnPage)

                if (data.players.size < 1000) break
                page++
            } catch (e: Exception) {
                if (e is GuildNotFoundException || e is LeaderboardNotPublicException || e is Mee6ApiException) {
                    throw e
                }
                e.printStackTrace()
                break
            }
        }

        return allUsers
    }

    fun close() {
        client.close()
    }
}
