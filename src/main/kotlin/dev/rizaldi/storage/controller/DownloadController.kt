package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("files/{id}")
class DownloadController(
    val gridFsTemplate: ReactiveGridFsTemplate
) {

    @GetMapping(
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun download(
        @PathVariable id: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        val query = Query(Criteria.where("_id").isEqualTo(id))
        return gridFsTemplate
            .findOne(query)
            .flatMap { file -> gridFsTemplate.getResource(file) }
            .map { resource ->
                ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "filename=" + resource.filename)
                    .body(resource.downloadStream)
            }
    }
}