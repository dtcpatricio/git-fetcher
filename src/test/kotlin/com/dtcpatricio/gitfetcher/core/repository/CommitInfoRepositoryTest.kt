package com.dtcpatricio.gitfetcher.core.repository

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit4.SpringRunner
import java.time.LocalDateTime

@RunWith(SpringRunner::class)
@SpringBootTest
class CommitInfoRepositoryTest {

    @Autowired
    private lateinit var subject: CommitInfoRepository

    @Test
    fun `findCommitsByRepository - if there exists commits for the repo return as a list`() {
        val repo = "octocat/octocat"
        val hash = "a1b2c3d4e5"
        val email = "octocat@locahost"
        val timestamp = LocalDateTime.now().minusDays(42)
        val title = "Changed something"

        val commit = subject.save(
            CommitInfo(repository = repo, hash = hash, email = email, timestamp = timestamp, title = title)
        )

        val commits = subject.findCommitsByRepository(repo)

        assertEquals(listOf(commit), commits)
    }

    @Test
    fun `findCommitsByRepository - if there are no commits for the repo then return empty list`() {
        assertEquals(
            emptyList<CommitInfo>(),
            subject.findCommitsByRepository("octocat/non-existent-repository")
        )
    }
}