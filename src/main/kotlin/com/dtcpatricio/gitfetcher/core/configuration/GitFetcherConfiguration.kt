package com.dtcpatricio.gitfetcher.core.configuration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class GitFetcherConfiguration {

    @Bean
    fun restTemplate() = RestTemplate()

    @Bean
    fun mapper() = jacksonObjectMapper().apply {
        findAndRegisterModules()
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}