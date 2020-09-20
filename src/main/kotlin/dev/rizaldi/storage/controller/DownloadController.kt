package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@RequestMapping("file")
class DownloadController(
    val gridFsTemplate: ReactiveGridFsTemplate
) {

    @GetMapping(
        consumes = [MediaType.ALL_VALUE],
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE]
    )
    fun download(
        @RequestParam id: String
    ): Flux<DataBuffer> {
        val query = Query(Criteria.where("_id").isEqualTo(id))
        return gridFsTemplate
            .findOne(query)
            .flatMap { file -> gridFsTemplate.getResource(file) }
            .flatMapMany { file -> file.downloadStream }
    }
}