package dev.rizaldi.storage

import dev.rizaldi.storage.handler.DownloadHandler
import dev.rizaldi.storage.handler.PartialDownloadHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates.GET
import org.springframework.web.reactive.function.server.RequestPredicates.headers
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions.route
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration
class Router {
    @Bean
    fun fileRoute(
        downloadHandler: DownloadHandler,
        partialDownloadHandler: PartialDownloadHandler
    ): RouterFunction<ServerResponse> {
        val downloadRoute = route(
            GET("/files/{id}").and(headers { h -> h.range().isNullOrEmpty() }),
            downloadHandler
        )
        val partialDownloadRoute = route(
            GET("/files/{id}").and(headers { h -> h.range().isNotEmpty() }),
            partialDownloadHandler
        )

        return route()
            .add(downloadRoute)
            .add(partialDownloadRoute)
            .build()
    }
}
