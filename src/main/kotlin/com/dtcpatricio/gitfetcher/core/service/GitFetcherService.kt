package com.dtcpatricio.gitfetcher.core.service

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import com.dtcpatricio.gitfetcher.core.repository.CommitInfoRepository
import com.dtcpatricio.gitfetcher.runner.api.ApiRunner
import com.dtcpatricio.gitfetcher.runner.cli.CliRunner
import org.apache.commons.logging.LogFactory
import org.springframework.stereotype.Component

@Component
class GitFetcherService(
    private val repository: CommitInfoRepository,
    private val apiRunner: ApiRunner,
    private val cliRunner: CliRunner
) {

    private val logger = LogFactory.getLog(this::class.java)

    fun fetch(url: String): List<CommitInfo>? {

        val repo = extractRepositoryName(url)
            ?: run { logger.error("repository url not suitable to extract its name"); return null }

        return try {
            // Try the Github API first
            var commits = apiRunner.run(repo)
                ?: repository.findCommitsByRepository(repo)
                ?: run { logger.error("commit_info table not correctly created"); return null }

            // If the API nor the repository worked then go fetch the commit list using git cli
            if (commits.isEmpty()) {
                commits = cliRunner.run(repo, url)
            }

            // Persist
            commits.takeIf(List<CommitInfo>::isNotEmpty)?.let { repository.saveAll(it) }

            // Return the populated commit list
            commits
        } catch (e: RuntimeException) {
            print(e.message)
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun extractRepositoryName(url: String) = Regex(".*:(.*)\\.git").find(url)?.destructured?.component1()
}