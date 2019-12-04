package com.dtcpatricio.gitfetcher.runner

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import com.dtcpatricio.gitfetcher.core.repository.CommitInfoRepository
import com.dtcpatricio.gitfetcher.core.service.GitFetcherService
import com.dtcpatricio.gitfetcher.runner.api.ApiRunner
import com.dtcpatricio.gitfetcher.runner.cli.CliRunner
import org.apache.commons.logging.LogFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import kotlin.system.exitProcess

@Component
@Profile("!test")
class Runner(
    private val fetcherService: GitFetcherService
) : CommandLineRunner {

    private val logger = LogFactory.getLog(this::class.java)

    override fun run(vararg args: String?) {
        if (args.isEmpty()) {
            println("Failed to provide a Git URL")
            usageAndExit()
        }

        val url = args[0]!!

        fetcherService.fetch(url)
            ?.let { printCommitList(it); print("DONE!") }
            ?: run { logger.error("Could not retrieve commit list. Review your args"); usageAndExit(-1) }

        exitProcess(0)
    }

    private fun usageAndExit(status: Int = 0) {
        println("GitFetcherApplication")
        println("Usage:")
        println("java -jar git-fetcher.jar <git-url>")
        exitProcess(status)
    }

    private fun printCommitList(list: List<CommitInfo>) {
        println("-- BEGIN OF COMMIT LIST --")
        for (c in list) {
            println("${c.timestamp} ${c.email} ${c.title}")
        }
        println("-- END OF COMMIT LIST --")
    }
}
