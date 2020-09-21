package dev.rizaldi.storage.files.handler

import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono
import java.util.*

@Component
class ListHandler(
    val gridFs: ReactiveGridFsTemplate
) : HandlerFunction<ServerResponse> {
    class ResponsePayload(
        val id: String,
        val name: String,
        val created_at: Date
    )

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val page = request.queryParam("page").orElse("0").toInt()
        val size = request.queryParam("size").orElse("10").toInt()
        val pageable = PageRequest.of(page, size, Sort.Direction.DESC, "uploadDate")

        val query = Query().with(pageable)
        val fFile = gridFs.find(query)
        val fPayload = fFile.map { file ->
            ResponsePayload(
                id = file.objectId.toHexString(),
                name = file.filename,
                created_at = file.uploadDate
            )
        }

        return ServerResponse
            .ok()
            .body(fPayload, ResponsePayload::class.java)
    }
}
