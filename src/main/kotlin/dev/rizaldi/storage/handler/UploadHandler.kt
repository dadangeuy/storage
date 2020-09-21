package dev.rizaldi.storage.handler

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import org.springframework.web.reactive.function.server.bodyToFlux
import reactor.core.publisher.Mono

@Component
class UploadHandler(
    val gridFs: ReactiveGridFsTemplate
) : HandlerFunction<ServerResponse> {
    class ResponsePayload(val id: String)

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val name = request.queryParam("name").orElseThrow()
        val fFile = request.bodyToFlux<DataBuffer>()

        val mPayload = gridFs
            .store(fFile, name)
            .map { id -> ResponsePayload(id = id.toHexString()) }
        val body = BodyInserters.fromPublisher(mPayload, ResponsePayload::class.java)

        return ServerResponse
            .ok()
            .body(body)
    }
}