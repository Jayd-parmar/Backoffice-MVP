package com.example.backofficemvp

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BackofficeMvpApplication

fun main(args: Array<String>) {
    runApplication<BackofficeMvpApplication>(*args)
    println("spring boot started")
}
