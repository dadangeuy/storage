package dev.rizaldi.storage.files

import dev.rizaldi.storage.files.handlers.ListHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.function.server.RequestPredicates
import org.springframework.web.reactive.function.server.RouterFunction
import org.springframework.web.reactive.function.server.RouterFunctions
import org.springframework.web.reactive.function.server.ServerResponse

@Configuration("files/router")
class Router {

    @Bean("files/router/list")
    fun list(handler: ListHandler): RouterFunction<ServerResponse> {
        return RouterFunctions.route(
            RequestPredicates.GET("/files"),
            handler
        )
    }
}
