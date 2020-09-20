package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("files")
class UploadController(
    val gridFs: ReactiveGridFsTemplate
) {
    class Response(val id: String)

    @PostMapping
    fun upload(
        @RequestParam name: String,
        @RequestBody fFile: Flux<DataBuffer>
    ): Mono<Response> {
        return gridFs
            .store(fFile, name)
            .map { id -> Response(id = id.toString()) }
    }
}
