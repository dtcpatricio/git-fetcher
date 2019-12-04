package com.dtcpatricio.gitfetcher.runner.cli

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import org.springframework.stereotype.Component
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.LinkOption.NOFOLLOW_LINKS
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
import java.util.regex.Pattern

val GIT_FETCH = listOf("git", "log", "--pretty=%H %ae %cI \"%s\"")
val GIT_CLONE = { url: String, path: String -> "git clone $url $path" }

@Component
class CliRunner {

    fun run(repo: String, url: String): List<CommitInfo> {
        val path = generatePathFromRepo(repo) ?: throw RuntimeException("Could not extract path from repository remote")

        if (!Files.exists(path, NOFOLLOW_LINKS)) {
            // Create directory structure since we're dealing with sub-directories
            path.toFile().mkdirs()
            // Clone the repo in this path
            val process = Runtime.getRuntime().exec(GIT_CLONE(url, path.toAbsolutePath().toString()))
            // wait for it ...
            process.waitFor()
            if (process.exitValue() < 0) {
                throw RuntimeException(" Bad value from \"git clone $url $path\". Check your filesystem permissions")
            }
        }

        // Use the Process API from Java to exec a system command.
        val process = execOnPath(GIT_FETCH, path.toFile())

        // Map the commits into a data structure
        // The data structure should be open to the class (a ref from the constructor)
        val list = parse(process.inputStream, repo) ?: throw RuntimeException("Could not create a commit list")

        process.waitFor()

        return list
    }

    private fun parse(inputStream: InputStream, repo: String): List<CommitInfo>? {
        val list = mutableListOf<CommitInfo>()
        val stdin = BufferedReader(InputStreamReader(inputStream))
        var line: String? = stdin.readLine()

        do {
            line?.split(Regex("( )(?=(([^\"]*\"){2}))"))?.let {
                val hash = it[0] // hash first
                val email = it[1] // email second
                val timestamp = LocalDateTime.parse(it[2], ISO_OFFSET_DATE_TIME) // timestamp third
                val title = it[3] // title fourth
                list.add(
                    CommitInfo(repository = repo, hash = hash, email = email, timestamp = timestamp, title = title)
                )
            } ?: return null
            line = stdin.readLine()
        } while (line != null)

        return list
    }

    private fun generatePathFromRepo(repo: String): Path? = FileSystems.getDefault().getPath("/tmp/$repo")

    private fun execOnPath(cmd: List<String>, path: File) =
        ProcessBuilder(cmd)
            .directory(path)
            .start()
}
