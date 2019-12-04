package com.dtcpatricio.gitfetcher.core.service

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import com.dtcpatricio.gitfetcher.core.repository.CommitInfoRepository
import com.dtcpatricio.gitfetcher.runner.api.ApiRunner
import com.dtcpatricio.gitfetcher.runner.cli.CliRunner
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDateTime

class GitFetcherServiceTest {

    private val commitInfoRepository = mock<CommitInfoRepository>()
    private val apiRunner = mock<ApiRunner>()
    private val cliRunner = mock<CliRunner>()

    private val subject = GitFetcherService(commitInfoRepository, apiRunner, cliRunner)

    private val repositoryUrl = "git@localhost:octocat/octocat.git"

    private val repo = "octocat/octocat"
    private val hash = "a1b2c3d5"
    private val email = "octocat@localhost"
    private val timestamp = LocalDateTime.now().minusDays(42)
    private val title = "Changed this"

    @Test
    fun `run - the public api is requested first and succeeds for public repository`() {
        val captor = argumentCaptor<String>()

        whenever(apiRunner.run("octocat/octocat"))
            .thenReturn(listOf(CommitInfo(null, repo, hash, email, timestamp, title)))

        subject.fetch(repositoryUrl)

        verify(apiRunner).run(captor.capture())
        verify(commitInfoRepository).saveAll(any<List<CommitInfo>>())
        verify(cliRunner, times(0)).run(any(), any())

        assertEquals("octocat/octocat", captor.firstValue)
    }

    @Test
    fun `run - network outages require that CliRunner is called if no commits are in the database`() {
        val captor = argumentCaptor<String>()

        whenever(apiRunner.run(any())).thenReturn(null)

        subject.fetch(repositoryUrl)

        verify(cliRunner).run(captor.capture(), eq(repositoryUrl))

        assertEquals("octocat/octocat", captor.firstValue)
    }

    @Test
    fun `run - if public api is down a user with the commits persisted in the database requires no use of git cli`() {
        whenever(apiRunner.run(any())).thenReturn(null)

        whenever(commitInfoRepository.findCommitsByRepository(repo))
            .thenReturn(listOf(CommitInfo(0, repo, hash, email, timestamp, title)))

        subject.fetch(repositoryUrl)

        verify(apiRunner).run(any())
        verify(cliRunner, times(0)).run(any(), any())
    }
}