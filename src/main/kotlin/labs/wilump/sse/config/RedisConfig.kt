package labs.wilump.sse.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration


@Configuration
class RedisConfig {

    @ConfigurationProperties(prefix = "spring.data.redis")
    fun redisProperties(): RedisProperties {
        return RedisProperties()
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisProperties = redisProperties()
        val redisStandaloneConfig = RedisStandaloneConfiguration(redisProperties.host, redisProperties.port)
        redisStandaloneConfig.database = redisProperties.database

        val lettuceClientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(Duration.ofMillis(redisProperties.timeout))
            .build()

        return LettuceConnectionFactory(redisStandaloneConfig, lettuceClientConfig)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val redisTemplate = RedisTemplate<String, String>()
        redisTemplate.connectionFactory = redisConnectionFactory()

        redisTemplate.keySerializer = StringRedisSerializer()
        redisTemplate.hashKeySerializer = StringRedisSerializer()
        redisTemplate.valueSerializer = StringRedisSerializer()
        redisTemplate.hashValueSerializer = StringRedisSerializer()

        return redisTemplate
    }

    @Bean
    fun redisMessageListener(): RedisMessageListenerContainer {
        val container = RedisMessageListenerContainer()
        container.setConnectionFactory(redisConnectionFactory())
        return container
    }
}

data class RedisProperties(
    var host: String = "localhost",
    var port: Int = 6379,
    var database: Int = 0,
    var timeout: Long = 2000,
)