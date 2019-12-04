package com.dtcpatricio.gitfetcher

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class GitFetcherApplication

fun main(args: Array<String>) {
    runApplication<GitFetcherApplication>(*args)
}
