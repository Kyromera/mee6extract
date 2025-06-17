package me.diamondforge.mee6extract.examples

import kotlinx.coroutines.runBlocking
import me.diamondforge.mee6extract.Mee6XpClient

object Mee6XpExample {
    @JvmStatic
    fun main(args: Array<String>) =
        runBlocking {
            val client = Mee6XpClient()

            try {
                val guildId = 123456789123456789

                println("Example 1: Get a single user's XP data")
                println("--------------------------------------")
                val userId = 987654321098765432L
                val userXp = client.getUserXp(guildId, userId)

                if (userXp != null) {
                    println("User: ${userXp.username}#${userXp.discriminator}")
                    println("Level: ${userXp.level}")
                    println("Total XP: ${userXp.totalXp}")
                    println("Rank: ${userXp.rank}")
                } else {
                    println("User not found or an error occurred")
                }

                println()

                println("Example 2: Get all users' XP data")
                println("----------------------------------")
                val allUsers = client.getAllUsers(guildId)

                if (allUsers.isNotEmpty()) {
                    println("Total users: ${allUsers.size}")
                    println("Top 5 users:")

                    allUsers.take(5).forEach { user ->
                        println("${user.rank}. ${user.username}#${user.discriminator} - Level: ${user.level}, XP: ${user.totalXp}")
                    }
                } else {
                    println("No users found or an error occurred")
                }

                println()

                println("Example 3: Get all user ranks from a guild")
                println("------------------------------------------")
                val allUserRanks = client.getAllUsers(guildId)

                if (allUserRanks.isNotEmpty()) {
                    println("Total users with ranks: ${allUserRanks.size}")
                    println("Top 5 users by rank:")

                    allUserRanks.take(5).forEach { user ->
                        println("${user.rank}. ${user.username}#${user.discriminator} - Level: ${user.level}")
                    }
                } else {
                    println("No user ranks found or an error occurred")
                }
            } finally {
                client.close()
            }
        }
}
