package com.dtcpatricio.gitfetcher.core.model

import org.springframework.data.annotation.Id
import java.time.LocalDateTime

data class CommitInfo(

    @Id
    val id: Long? = null,
    
    val repository: String,
    
    val hash: String,
    
    val email: String,
    
    val timestamp: LocalDateTime,
    
    val title: String
)
