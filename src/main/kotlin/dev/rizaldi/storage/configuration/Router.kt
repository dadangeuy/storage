package dev.rizaldi.storage.configuration

import dev.rizaldi.storage.handler.DownloadHandler
import dev.rizaldi.storage.handler.PartialDownloadHandler
import dev.rizaldi.storage.handler.UploadHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class Router {

    @Bean
    fun downloadRoute(handler: DownloadHandler): RouterFunction<ServerResponse> {
        return route(
            GET("/files/{id}").and(headers { h -> h.range().isNullOrEmpty() }),
            handler
        )
    }

    @Bean
    fun partialDownloadRoute(handler: PartialDownloadHandler): RouterFunction<ServerResponse> {
        return route(
            GET("/files/{id}").and(headers { h -> h.range().isNotEmpty() }),
            handler
        )
    }

    @Bean
    fun uploadRoute(handler: UploadHandler): RouterFunction<ServerResponse> {
        return route(
            POST("/files").and(queryParam("name") { p -> p.isNotBlank() }),
            handler
        )
    }
}
