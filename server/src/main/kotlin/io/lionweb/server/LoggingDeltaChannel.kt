package io.lionweb.server

import com.google.gson.Gson
import io.lionweb.client.delta.DeltaChannel
import io.lionweb.client.delta.DeltaCommandReceiver
import io.lionweb.client.delta.DeltaEventReceiver
import io.lionweb.client.delta.DeltaMessageSerialization
import io.lionweb.client.delta.DeltaQueryReceiver
import io.lionweb.client.delta.DeltaQueryResponseReceiver
import io.lionweb.client.delta.messages.DeltaCommand
import io.lionweb.client.delta.messages.DeltaEvent
import io.lionweb.client.delta.messages.DeltaQuery
import io.lionweb.client.delta.messages.DeltaQueryResponse
import io.lionweb.client.delta.messages.ProtocolMessage
import java.util.function.Function

class LoggingDeltaChannel(
    private val delegate: DeltaChannel,
    private val log: MessageLog,
    private val serialization: DeltaMessageSerialization,
) : DeltaChannel {
    private val gson = Gson()

    override fun sendQuery(queryProducer: Function<String, DeltaQuery>): DeltaQueryResponse? {
        val loggingProducer =
            Function<String, DeltaQuery> { id ->
                val q = queryProducer.apply(id)
                log.add(
                    MessageLogEntry(
                        timestamp = System.currentTimeMillis(),
                        direction = "sent",
                        category = "query",
                        messageKind = q.javaClass.simpleName,
                        json = trySer(q),
                    ),
                )
                q
            }
        val response = delegate.sendQuery(loggingProducer)
        if (response != null) {
            log.add(
                MessageLogEntry(
                    timestamp = System.currentTimeMillis(),
                    direction = "received",
                    category = "response",
                    messageKind = response.javaClass.simpleName,
                    json = trySer(response),
                ),
            )
        }
        return response
    }

    override fun sendCommand(
        participationId: String,
        commandProducer: Function<String, DeltaCommand>,
    ) {
        val loggingProducer =
            Function<String, DeltaCommand> { id ->
                val c = commandProducer.apply(id)
                log.add(
                    MessageLogEntry(
                        timestamp = System.currentTimeMillis(),
                        direction = "sent",
                        category = "command",
                        messageKind = c.javaClass.simpleName,
                        participationId = participationId,
                        json = trySer(c),
                    ),
                )
                c
            }
        delegate.sendCommand(participationId, loggingProducer)
    }

    override fun sendEvent(eventProducer: Function<Int, DeltaEvent>) = delegate.sendEvent(eventProducer)

    override fun registerEventReceiver(r: DeltaEventReceiver) = delegate.registerEventReceiver(r)

    override fun unregisterEventReceiver(r: DeltaEventReceiver) = delegate.unregisterEventReceiver(r)

    override fun registerCommandReceiver(r: DeltaCommandReceiver) = delegate.registerCommandReceiver(r)

    override fun unregisterCommandReceiver(r: DeltaCommandReceiver) = delegate.unregisterCommandReceiver(r)

    override fun registerQueryReceiver(r: DeltaQueryReceiver) = delegate.registerQueryReceiver(r)

    override fun unregisterQueryReceiver(r: DeltaQueryReceiver) = delegate.unregisterQueryReceiver(r)

    override fun registerQueryResponseReceiver(r: DeltaQueryResponseReceiver) {
        delegate.registerQueryResponseReceiver(r)
    }

    override fun unregisterQueryResponseReceiver(r: DeltaQueryResponseReceiver) {
        delegate.unregisterQueryResponseReceiver(r)
    }

    private fun trySer(msg: Any): String =
        try {
            serialization.serialize(msg as? ProtocolMessage ?: return gson.toJson(msg))
        } catch (_: Exception) {
            gson.toJson(msg)
        }
}
