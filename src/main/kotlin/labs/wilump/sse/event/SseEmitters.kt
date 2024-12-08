package labs.wilump.sse.event

import com.google.gson.Gson
import jakarta.annotation.PostConstruct
import labs.wilump.sse.logger
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.CopyOnWriteArrayList

@Component
class SseEmitters(
    private val listener: RedisMessageListenerContainer,
    private val publisher: RedisPublisher,
) : MessageListener {
    private val log by logger()
    private val gson = Gson()

    private val emitters = CopyOnWriteArrayList<SseEmitter>()

    @PostConstruct
    fun init() {
        listener.addMessageListener(this, ChannelTopic(Topic.EMITTER))
    }

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
        publisher.publishEmitterEvent(EmitterEventMessage(event.first, event.second))
    }

    override fun onMessage(message: Message, pattern: ByteArray?) {
        log.info("received message: {}", message)
        val event = gson.fromJson(message.toString(), EmitterEventMessage::class.java)
        handleEvent(event)
    }

    private fun handleEvent(message: EmitterEventMessage) {
        log.info("handling event: {}", message)
        broadcastSelf(message)
    }

    private fun broadcastSelf(event: EmitterEventMessage) {
        log.info("internal broadcast: {}", event)
        emitters.forEach {
            try {
                it.send(SseEmitter.event().name(event.key).data(event.message))
            } catch (e: Exception) {
                log.error("Error sending message to emitter: {}", it, e)
                throw IllegalStateException("Error sending message to emitter: $it", e)
            }
        }
    }
}