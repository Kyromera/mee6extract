package me.diamondforge.mee6.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Mee6Response(
    val players: List<Mee6User>? = null,
)

@Serializable
data class Mee6User(
    val id: Long,
    val username: String,
    val discriminator: String,
    val level: Int,
    @SerialName("detailed_xp") val detailedXp: List<Long>,
)

data class UserXp(
    val id: Long,
    val username: String,
    val discriminator: String,
    val level: Int,
    val totalXp: Long,
    val rank: Int,
)
