package labs.wilump.sse.controller

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExecutorCoroutineDispatcher
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import labs.wilump.sse.logger
import org.springframework.http.MediaType
import org.springframework.http.client.JdkClientHttpRequestFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import java.net.http.HttpClient
import java.util.concurrent.Executors

val Dispatchers.VIRTUAL_THREAD: ExecutorCoroutineDispatcher
    get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

@RestController
@RequestMapping("/price")
class PriceController {
    private val log by logger()

    private val restClient by lazy {
        RestClient.builder()
            .baseUrl("https://crix-api-cdn.upbit.com/v1/crix/trades/days?code=CRIX.UPBIT.KRW-BTC&count=1&convertingPriceUnit=KRW")
            .requestFactory(
                JdkClientHttpRequestFactory(
                    HttpClient.newBuilder()
                        .executor(Executors.newVirtualThreadPerTaskExecutor())
                        .build()
                )
            )
            .build()
    }

    private fun getPrice(): Double {
        val response = restClient.get()
            .retrieve()
            .toEntity(List::class.java)

        val tradePrice = (response.body as List<*>).firstOrNull()?.let {
            (it as Map<*, *>)["tradePrice"] as Double?
        } ?: 0.0

        log.info("Current trade price: $tradePrice")

        return tradePrice
    }

    @GetMapping("/recent", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun price(): Flow<Map<String, Any>> {
        return flow {
            while (true) {
                val price = getPrice()
                val data = mapOf("krw-btc" to String.format("%.0f", price))
                emit(data)
                delay(5000)
            }
        }.flowOn(Dispatchers.VIRTUAL_THREAD)
    }

    @GetMapping("/threads")
    fun thread(): String {
        val currThread = Thread.currentThread()
        log.info("Current thread: $currThread")
        return currThread.name
    }
}