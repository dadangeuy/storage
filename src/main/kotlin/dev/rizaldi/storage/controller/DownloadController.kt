package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.MediaTypeFactory
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

    @GetMapping
    fun download(
        @PathVariable id: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        val query = Query(Criteria.where("_id").isEqualTo(id))
        val mFile = gridFsTemplate.findOne(query)
        val mResource = mFile.flatMap { file -> gridFsTemplate.getResource(file) }

        return mResource.map { resource ->
            val mediaType = MediaTypeFactory
                .getMediaType(resource.filename)
                .orElse(MediaType.APPLICATION_OCTET_STREAM)

            ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "filename=" + resource.filename)
                .contentType(mediaType)
                .body(resource.downloadStream)
        }
    }
}