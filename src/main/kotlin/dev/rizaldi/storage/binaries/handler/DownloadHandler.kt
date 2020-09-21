package dev.rizaldi.storage.binaries.handler

import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.server.HandlerFunction
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.reactive.function.server.ServerResponse
import reactor.core.publisher.Mono

@Component
class DownloadHandler(
    val gridFs: ReactiveGridFsTemplate
) : HandlerFunction<ServerResponse> {

    override fun handle(request: ServerRequest): Mono<ServerResponse> {
        val id = request.pathVariable("id")

        val query = Query(Criteria.where("_id").isEqualTo(id))
        val mFile = gridFs.findOne(query)
        val mResource = mFile.flatMap { file -> gridFs.getResource(file) }

        return mResource.flatMap { resource ->
            val contentDisposition = ContentDisposition
                .builder("inline")
                .filename(resource.filename)
                .build()

            val contentType = MediaTypeFactory
                .getMediaType(resource.filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)

            val stream = resource.downloadStream
            val body = BodyInserters.fromDataBuffers(stream)

            ServerResponse
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                .contentType(contentType)
                .body(body)
        }
    }
}
