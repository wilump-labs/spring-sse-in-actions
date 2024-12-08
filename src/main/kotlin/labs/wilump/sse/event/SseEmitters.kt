package labs.wilump.sse.event

import labs.wilump.sse.logger
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Component
class SseEmitters {
    private val log by logger()

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    fun size(): Int {
        return emitters.size
    }

    fun join(emitter: SseEmitter = createEmitter()): SseEmitter {
        this.emitters.add(emitter)
        log.info("new emitter added: {}, size: {}, list: {}", emitter, emitters.size, emitters)

        // emitter callbacks (연결 종료 및 핸들링 로직 추가)
        emitter.onCompletion {
            log.info("onCompletion callback")
            emitters.remove(emitter)
        }
        emitter.onTimeout {
            log.info("onTimeout callback")
            emitter.complete()
        }

        return emitter
    }

    private fun createEmitter(): SseEmitter {
        val emitter = SseEmitter(60 * 60 * 1000L) // default timeout of 30 seconds
        return emitter
    }

    fun broadcast(event: Pair<String, Any>) {
        log.info("broadcasting event. key: {}, message: {}", event.first, event.second)
        emitters.forEach {
            try {
                it.send(SseEmitter.event().name(event.first).data(event.second))
            } catch (e: Exception) {
                log.error("Error sending message to emitter: {}", it, e)
                throw IllegalStateException("Error sending message to emitter: $it", e)
            }
        }
    }
}