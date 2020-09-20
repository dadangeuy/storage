package dev.rizaldi.storage.controller

import org.springframework.core.io.buffer.DataBuffer
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.isEqualTo
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate
import org.springframework.http.*
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@RequestMapping("files/{id}")
class DownloadController(
    val gridFs: ReactiveGridFsTemplate
) {

    @GetMapping
    fun download(
        @RequestHeader headers: HttpHeaders,
        @PathVariable id: String
    ): Mono<ResponseEntity<Flux<DataBuffer>>> {
        val query = Query(Criteria.where("_id").isEqualTo(id))
        val mFile = gridFs.findOne(query)
        val mResource = mFile.flatMap { file -> gridFs.getResource(file) }

        return if (headers.range.isNullOrEmpty()) {
            mResource.map { resource ->
                val contentDisposition = ContentDisposition
                    .builder("inline")
                    .filename(resource.filename)
                    .build()
                val mediaType = MediaTypeFactory
                    .getMediaType(resource.filename)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM)

                ResponseEntity
                    .ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString())
                    .contentType(mediaType)
                    .body(resource.downloadStream)
            }
        } else {
            mFile.zipWith(mResource).map { t ->
                val file = t.t1
                val resource = t.t2

                val byteStartAt = headers.range.first().getRangeStart(file.length)
                val byteChunkStartAt = byteStartAt - (byteStartAt % file.chunkSize)
                val chunkStartAt = byteChunkStartAt / file.chunkSize

                val contentRange = String.format("bytes %d-%d/%d", byteChunkStartAt, file.length - 1, file.length)
                val mediaType = MediaTypeFactory
                    .getMediaType(resource.filename)
                    .orElse(MediaType.APPLICATION_OCTET_STREAM)
                val stream = resource.downloadStream.skip(chunkStartAt)

                ResponseEntity
                    .status(HttpStatus.PARTIAL_CONTENT)
                    .header(HttpHeaders.CONTENT_RANGE, contentRange)
                    .contentType(mediaType)
                    .body(stream)
            }
        }
    }
}
