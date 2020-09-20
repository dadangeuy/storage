package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("file")
class UploadController(
    val gridFsTemplate: ReactiveGridFsTemplate
) {
    class Response(val id: String)

    @PostMapping(
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_STREAM_JSON_VALUE]
    )
    fun upload(
        @RequestParam name: String,
        @RequestBody file_chunks: Flux<DataBuffer>
    ): Mono<Response> {
        return gridFsTemplate
            .store(file_chunks, name)
            .map { id -> Response(id = id.toString()) }
    }
}
