package labs.wilump.sse.event

data class EmitterEventMessage(
    val key: String,
    val message: Any,
)
