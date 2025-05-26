package labs.wilump.sse.controller

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import labs.wilump.sse.logger
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestClient
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.math.BigDecimal
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


val Dispatchers.VIRTUAL_THREAD: ExecutorCoroutineDispatcher
    get() = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

fun <T> CoroutineScope.asyncOnVirtualThread(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> T,
): Deferred<T> {
    return async(Dispatchers.VIRTUAL_THREAD + context, start, block)
}

fun CoroutineScope.launchOnVirtualThread(
    context: CoroutineContext = EmptyCoroutineContext,
    start: CoroutineStart = CoroutineStart.DEFAULT,
    block: suspend CoroutineScope.() -> Unit,
): Job {
    return launch(Dispatchers.VIRTUAL_THREAD + context, start, block)
}

@RestController
@RequestMapping("/price")
class PriceController {
    private val log by logger()

    private val restClient by lazy {
        RestClient.builder()
            .baseUrl("https://crix-api-cdn.upbit.com/v1/crix/trades/days?code=CRIX.UPBIT.KRW-BTC&count=1&convertingPriceUnit=KRW")
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
                delay(1000)
            }
        }.flowOn(Dispatchers.VIRTUAL_THREAD)
    }
}