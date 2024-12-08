package labs.wilump.sse.controller

import labs.wilump.sse.event.SseEmitters
import labs.wilump.sse.logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/event")
class EventController(
    private val emitters: SseEmitters,
) {
    private val log by logger()

    @GetMapping("/connect", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connect(): ResponseEntity<SseEmitter> {
        val newEmitter = emitters.join()
        newEmitter.send(SseEmitter.event().name("connect").data("connected"))
        return ResponseEntity.ok(newEmitter)
    }

    @GetMapping("/count")
    fun count(): ResponseEntity<Void> {
        emitters.broadcast("count" to emitters.size())
        return ResponseEntity.ok().build();
    }

    @GetMapping("/message")
    fun message(): ResponseEntity<Void> {
        data class SampleMessageFormat(
            val content: String,
            val timestamp: Long = System.currentTimeMillis()
        )
        val randMessage = SampleMessageFormat("Hello, World!")
        emitters.broadcast("message" to randMessage)
        return ResponseEntity.ok().build();
    }
}