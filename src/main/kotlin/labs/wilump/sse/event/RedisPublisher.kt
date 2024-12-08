package labs.wilump.sse.event

import com.google.gson.Gson
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component


@Component
class RedisPublisher(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    private val gson = Gson()

    fun publish(topic: ChannelTopic, message: EmitterEventMessage) {
        redisTemplate.convertAndSend(topic.topic, gson.toJson(message))
    }

    fun publishEmitterEvent(message: EmitterEventMessage) {
        redisTemplate.convertAndSend(ChannelTopic(Topic.EMITTER).topic, gson.toJson(message))
    }
}