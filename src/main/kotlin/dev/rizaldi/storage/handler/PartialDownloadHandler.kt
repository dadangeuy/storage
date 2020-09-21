package dev.rizaldi.storage.handler

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class PartialDownloadHandler(
    val gridFs: ReactiveGridFsTemplate
) : HandlerFunction<ServerResponse> {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")
        val range = request.headers().range().first()

        val query = Query(Criteria.where("_id").isEqualTo(id))
        val mFile = gridFs.findOne(query)
        val mResource = mFile.flatMap { file -> gridFs.getResource(file) }

        return mFile.zipWith(mResource).flatMap { t ->
            val file = t.t1
            val resource = t.t2

            val maxChunk = 32L
            val byteStartAt = range.getRangeStart(file.length)
            val byteChunkStartAt = byteStartAt - (byteStartAt % file.chunkSize)
            val byteChunkEndAt = minOf(file.length - 1, byteChunkStartAt + maxChunk * file.chunkSize)
            val chunkStartIdx = byteChunkStartAt / file.chunkSize

            val contentRange = String.format("bytes %d-%d/%d", byteChunkStartAt, byteChunkEndAt, file.length)

            val stream = resource.downloadStream
                .skip(chunkStartIdx)
                .take(maxChunk)
            val body = BodyInserters.fromDataBuffers(stream)

            ServerResponse
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE, contentRange)
                .body(body)
        }
    }
}
