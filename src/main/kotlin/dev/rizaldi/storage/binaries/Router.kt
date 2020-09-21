package dev.rizaldi.storage.binaries

import dev.rizaldi.storage.binaries.handler.DownloadHandler
import dev.rizaldi.storage.binaries.handler.PartialDownloadHandler
import dev.rizaldi.storage.binaries.handler.UploadHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.*
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration("binaries/router")
class Router {

    @Bean("binaries/router/upload")
    fun upload(handler: UploadHandler): RouterFunction<ServerResponse> {
        return route(
            POST("/files/binaries").and(queryParam("name") { p -> p.isNotBlank() }),
            handler
        )
    }

    @Bean("binaries/router/download")
    fun download(handler: DownloadHandler): RouterFunction<ServerResponse> {
        return route(
            GET("/files/{id}/binaries").and(headers { h -> h.range().isNullOrEmpty() }),
            handler
        )
    }

    @Bean("binaries/router/partialDownload")
    fun partialDownload(handler: PartialDownloadHandler): RouterFunction<ServerResponse> {
        return route(
            GET("/files/{id}/binaries").and(headers { h -> h.range().isNotEmpty() }),
            handler
        )
    }
}
