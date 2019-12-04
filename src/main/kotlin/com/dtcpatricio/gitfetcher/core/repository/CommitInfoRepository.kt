package com.dtcpatricio.gitfetcher.core.repository

import com.dtcpatricio.gitfetcher.core.model.CommitInfo
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param

// FIXME
// const val EXISTS_QUERY =
//     "SELECT EXISTS(" +
//       "SELECT 1 FROM pg_tables WHERE schemaname = 'public' AND tablename = \':name\'" +
//     ");"

interface CommitInfoRepository : CrudRepository<CommitInfo, Long> {

    @Query("select * from commit_info where repository = :repository")
    fun findCommitsByRepository(@Param("repository") repository: String): List<CommitInfo>?
}
