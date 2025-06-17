# MEE6 Extract

A Kotlin library for extracting XP and level data from the MEE6 Discord bot.

## Features

- Get XP and level data for a specific user in a Discord guild
- Get XP and level data for all users in a Discord guild
- Retrieve user rankings based on XP

## Installation

### Gradle (Kotlin DSL)

```kotlin
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.kyromera:mee6extract:1.0.0")
}
```

### Maven

```xml
<dependency>
    <groupId>com.github.kyromera</groupId>
    <artifactId>mee6extract</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

### Basic Usage

```kotlin
fun main() {
    // Using runBlocking from kotlinx.coroutines
    val client = Mee6XpClient()

    try {
        // Replace with your Discord guild ID
        val guildId = 123456789012345678

        // Get XP data for a specific user
        val userId = 987654321098765432
        val userXp = client.getUserXp(guildId, userId)

        if (userXp != null) {
            println("User: ${userXp.username}#${userXp.discriminator}")
            println("Level: ${userXp.level}")
            println("Total XP: ${userXp.totalXp}")
            println("Rank: ${userXp.rank}")
        }

        // Get XP data for all users in the guild
        val allUsers = client.getAllUsers(guildId)

        allUsers.take(5).forEach { user ->
            println("${user.rank}. ${user.username}#${user.discriminator} - Level: ${user.level}, XP: ${user.totalXp}")
        }
    } finally {
        client.close()
    }
}
```

### Custom HTTP Client

You can provide your own Ktor HTTP client with custom configurations:

```kotlin
// Create a custom Ktor HTTP client
val customClient = HttpClient(/* CIO engine */) {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            isLenient = true
            // Add any other JSON configuration
        })
    }
    // Add any other client configuration
}

val mee6Client = Mee6XpClient(customClient)
```

## Data Models

The library provides the following data models:

- `UserXp`: Contains processed user data with:
  - `id`: Discord user ID
  - `username`: Discord username
  - `discriminator`: Discord discriminator (should be 0 for users, and bots cant level, but retain it for compatibility with the api)
  - `level`: MEE6 level
  - `totalXp`: Total XP accumulated
  - `rank`: User's rank in the guild

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
