package com.dtcpatricio.gitfetcher.runner.api

import com.dtcpatricio.gitfetcher.core.gateway.GithubApiGateway
import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import org.springframework.stereotype.Component

@Component
class ApiRunner(
    private val gateway: GithubApiGateway
) {

    fun run(repo: String): List<CommitInfo>? = gateway.fetch(repo)
}