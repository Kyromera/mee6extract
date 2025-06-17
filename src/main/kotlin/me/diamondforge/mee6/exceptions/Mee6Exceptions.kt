package me.diamondforge.mee6.exceptions


sealed class Mee6Exception(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)

class GuildNotFoundException(
    guildId: Long,
    message: String = "Guild not found",
) : Mee6Exception("Guild with ID $guildId not found: $message")


class LeaderboardNotPublicException(
    guildId: Long,
    message: String = "Leaderboard not public",
) : Mee6Exception("Leaderboard for guild with ID $guildId is not public: $message")


class Mee6ApiException(
    statusCode: Int,
    message: String,
) : Mee6Exception("MEE6 API error (status code $statusCode): $message")
