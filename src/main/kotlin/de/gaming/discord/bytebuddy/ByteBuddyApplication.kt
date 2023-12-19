package de.gaming.discord.bytebuddy

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ByteBuddyApplication

fun main(args: Array<String>) {
    runApplication<ByteBuddyApplication>(*args)
}
