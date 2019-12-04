package com.dtcpatricio.gitfetcher.core.gateway

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.commons.logging.LogFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.ACCEPT
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import java.net.URI
import java.time.LocalDateTime

@Component
class GithubApiGateway(
    private val mapper: ObjectMapper,
    private val template: RestTemplate
) {

    private val logger = LogFactory.getLog(this::class.java)

    private val apiRootUrl = "https://api.github.com"
    private val commitsEndpoint = { path: String -> "$apiRootUrl/repos/$path/commits" }

    fun fetch(repo: String): List<CommitInfo>? {
        return try {
            val response = callApi<String>(endpoint = commitsEndpoint(repo))

            response.body
                .takeIf { response.statusCode.is2xxSuccessful }
                ?.let {
                    parse(mapper.readValue(it), repo)
                } ?: run { logger.warn("Empty body"); null }
        } catch(e: RestClientException) {
            logger.warn("Failed to obtain commit list due to: ${e.message}")
            null
        } catch(e: JsonMappingException) {
            logger.warn("Failed to map JSON to Commit object: ${e.message}")
            null
        }
    }

    private inline fun <reified T> callApi(
        endpoint: String,
        method: HttpMethod = HttpMethod.GET,
        responseType: Class<T> = T::class.java
    ): ResponseEntity<T> {
        val headers = HttpHeaders().apply { set(ACCEPT, "application/vnd.github.v3+json") }

        val request = RequestEntity<Void>(
            headers,
            method,
            URI(endpoint)
        )

        return template.exchange(request, responseType)
    }

    private fun parse(list: List<CommitPayload>, repo: String): List<CommitInfo> {
        return list.map { CommitInfo(
            repository = repo,
            hash = it.sha,
            email = it.commit.author.email,
            timestamp = it.commit.author.date,
            title = it.commit.message
        )}
    }
}

data class CommitPayload(val sha: String, val commit: Commit)
data class CommitAuthor(val email: String, val date: LocalDateTime)
data class Commit(val author: CommitAuthor, val message: String)
