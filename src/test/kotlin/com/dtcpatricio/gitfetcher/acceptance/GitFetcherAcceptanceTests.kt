package com.dtcpatricio.gitfetcher.acceptance

import com.dtcpatricio.gitfetcher.core.gateway.Commit
import com.dtcpatricio.gitfetcher.core.gateway.CommitAuthor
import com.dtcpatricio.gitfetcher.core.gateway.CommitPayload
import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import com.dtcpatricio.gitfetcher.core.repository.CommitInfoRepository
import com.dtcpatricio.gitfetcher.core.service.GitFetcherService
import com.dtcpatricio.gitfetcher.runner.api.ApiRunner
import com.dtcpatricio.gitfetcher.runner.cli.CliRunner
import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockitokotlin2.*
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.SpyBean
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.client.RestTemplate
import java.lang.Exception
import java.lang.RuntimeException
import java.time.LocalDateTime
import javax.xml.ws.Response

/**
 * Acceptance tests for the application. These try to assert the use case required for the system
 * (very similar to GitFetcherServiceTest {@link com.dtcpatricio.gitfetcher.core.service.GitFetcherServiceTest}.
 *  NB: We're not running a real CliRunner since this one is hard to mock due to its use of the Process API
 *  @see com.dtcpatricio.gitfetcher.runner.cli.CliRunner
 */
@RunWith(SpringRunner::class)
@SpringBootTest
class GitFetcherAcceptanceTests {

    @Autowired
    private lateinit var fetcherService: GitFetcherService

    @Autowired
    private lateinit var mapper: ObjectMapper

    @Autowired
    private lateinit var commitInfoRepository: CommitInfoRepository

    @SpyBean
    private lateinit var apiRunner: ApiRunner

    @MockBean
    private lateinit var cliRunner: CliRunner

    @MockBean
    private lateinit var restTemplate: RestTemplate

    private val repositoryUrl = "git@localhost:octocat/octocat.git"
    private val repo = "octocat/octocat"
    private val hash = "a1b2c3d4"
    private val email = "octocat@localhost"
    private val timestamp = LocalDateTime.now().minusDays(42)
    private val title = "Changed this"

    @Before
    fun setup() {
        commitInfoRepository.deleteAll()

        whenever(cliRunner.run(any(), any())).thenReturn(
            listOf(CommitInfo(repository = repo, hash = hash, email = email, timestamp = timestamp, title = title))
        )
    }

    @Test
    fun `the github public api is first used to fetch commits`() {
        val expectedCommit = CommitPayload(hash, Commit(CommitAuthor(email = email, date = timestamp), title))
        val apiResponse = ResponseEntity.ok(mapper.writeValueAsString(listOf(expectedCommit)))

        whenever(restTemplate.exchange(any<RequestEntity<Void>>(), any<Class<String>>()))
            .thenReturn(apiResponse)

        val commits = fetcherService.fetch(repositoryUrl)!!

        assert(commits.isNotEmpty())

        verify(apiRunner).run(repo)
        verify(cliRunner, times(0)).run(any(), any())
    }

    @Test
    fun `the git cli is used to fetch commits if the github public api fails`() {
        whenever(restTemplate.exchange(any<RequestEntity<Void>>(), any<Class<String>>()))
            .thenReturn(ResponseEntity.notFound().build())

        val commits = fetcherService.fetch(repositoryUrl)!!

        assert(commits.isNotEmpty())

        verify(cliRunner).run(repo, repositoryUrl)
    }

    @Test
    fun `if public api is down an already available list of commits in the db for a given repo avoids using the cli`() {
        whenever(restTemplate.exchange(any<RequestEntity<Void>>(), any<Class<String>>()))
            .thenReturn(ResponseEntity.notFound().build())

        commitInfoRepository.save(
            CommitInfo(repository = repo, hash = hash, email = email, timestamp = timestamp, title = title)
        )

        val commits = fetcherService.fetch(repositoryUrl)!!

        assert(commits.isNotEmpty())

        verify(apiRunner).run(any())
        verify(cliRunner, times(0)).run(any(), any())
    }

    @Test
    fun `if an exception is thrown by the api runner return void commits`() {
        doThrow(RuntimeException("error")).`when`(apiRunner).run(repo)
        assertNull(fetcherService.fetch(repositoryUrl))
    }

    @Test
    fun `if an exception is thrown by the cli runner return void commits`() {
        whenever(cliRunner.run(repo, repositoryUrl)).thenThrow(RuntimeException("error"))
        assertNull(fetcherService.fetch(repositoryUrl))
    }
}