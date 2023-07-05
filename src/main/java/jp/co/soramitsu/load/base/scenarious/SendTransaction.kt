package jp.co.soramitsu.load.base.scenarious

import com.fasterxml.jackson.databind.module.SimpleModule
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.jackson.*
import io.ktor.websocket.*
import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.*
import jp.co.soramitsu.iroha2.transaction.Filters
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.time.withTimeout
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

open class SendTransaction {

    val peerUrl = "http://127.0.0.1:8080"
    val telemetryUrl = "http://127.0.0.1:8180"
    val instructions: Lazy<ArrayList<InstructionBox>>
    open val log: Boolean = false
    open val credentials: String? = null
    open val eventReadMaxAttempts: Int = 10
    open val eventReadTimeoutInMills: Long = 250

    init {
        instructions = lazy { ArrayList() }
    }

    open val client by lazy {
        HttpClient(CIO) {
            expectSuccess = true
            if (log) {
                install(Logging)
            }
            install(WebSockets)
            install(ContentNegotiation) {
                jackson {
                    registerModule(
                        SimpleModule().apply {
                            addDeserializer(Duration::class.java, Iroha2Client.DurationDeserializer)
                        }
                    )
                }
            }
            credentials?.split(":")?.takeIf { it.size == 2 }?.let { pair ->
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(pair[0], pair[1])
                        }
                    }
                }
            }
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    val status = exception
                        .takeIf { it is ClientRequestException }
                        ?.cast<ClientRequestException>()
                        ?.response?.status
                    throw IrohaClientException(cause = exception, status = status)
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun sendNewTransaction(transaction: VersionedSignedTransaction) =
            runBlocking { SendTransaction().sendTransaction(transaction)
                .also {
                    d -> withTimeout(Duration.ofSeconds(5)) { d.await() }
                }
            }
    }

    open val logger: Logger = LoggerFactory.getLogger(javaClass)

    fun fireAndForget(transaction: TransactionBuilder.() -> VersionedSignedTransaction): ByteArray {
        val signedTransaction = transaction(TransactionBuilder.builder())
        val hash = signedTransaction.hash()
        logger.debug("Sending transaction with hash {}", hash.toHex())
        val response: ByteArray = VersionedSignedTransaction.encode(signedTransaction)
        return response
    }

   /* suspend fun sendTransaction(
        client: Iroha2Client,
        transaction: VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val lock = Mutex(locked = true)

        client.subscribeToTransactionStatus(transaction.hash())
            .also {
                lock.unlock()
                lock.lock()
                fireAndForget { transaction }
            }
    }*/
   suspend fun sendTransaction(
        transaction: VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val lock = Mutex(locked = true)

        subscribeToTransactionStatus(transaction.hash()){
            lock.unlock()
        }
            .also {
                lock.lock()
                fireAndForget { transaction }
            }
    }

    private fun subscribeToTransactionStatus(
        hash: ByteArray,
        afterSubscription: (() -> Unit)? = null
    ): CompletableDeferred<ByteArray> {
        val hexHash = hash.toHex()
        logger.debug("Creating subscription to transaction status: {}", hexHash)

        val subscriptionRequest = eventSubscriberMessageOf(hash)
        val payload = VersionedEventSubscriptionRequest.encode(subscriptionRequest)
        val result: CompletableDeferred<ByteArray> = CompletableDeferred()

        launch {
            client.webSocket(
                host = peerUrl,
                path = Iroha2Client.WS_ENDPOINT
            ) {
                logger.debug("WebSocket opened")
                send(payload.toFrame())

                afterSubscription?.invoke()
                logger.debug("Subscription was accepted by peer")

                for (i in 1..eventReadMaxAttempts) {
                    try {
                        val processed = pipelineEventProcess(readMessage(incoming.receive()), hash, hexHash)
                        if (processed != null) {
                            result.complete(processed)
                            break
                        }
                    } catch (e: TransactionRejectedException) {
                        result.completeExceptionally(e)
                        break
                    }
                    delay(eventReadTimeoutInMills)
                }
            }
        }
        return result
    }

    private fun pipelineEventProcess(
        eventPublisherMessage: EventMessage,
        hash: ByteArray,
        hexHash: String
    ): ByteArray? {
        when (val event = eventPublisherMessage.event) {
            is Event.Pipeline -> {
                val eventInner = event.pipelineEvent
                if (eventInner.entityKind is PipelineEntityKind.Transaction && hash.contentEquals(eventInner.hash.arrayOfU8)) {
                    when (val status = eventInner.status) {
                        is PipelineStatus.Committed -> {
                            logger.debug("Transaction {} committed", hexHash)
                            return hash
                        }

                        is PipelineStatus.Rejected -> {
                            val reason = status.pipelineRejectionReason.toString()
                            logger.error("Transaction {} was rejected by reason: `{}`", hexHash, reason)
                            throw TransactionRejectedException("Transaction rejected with reason '$reason'")
                        }

                        is PipelineStatus.Validating -> logger.debug("Transaction {} is validating", hexHash)
                    }
                }
                return null
            }

            else -> throw WebSocketProtocolException(
                "Expected message with type ${Event.Pipeline::class.qualifiedName}, " +
                        "but was ${event::class.qualifiedName}"
            )
        }
    }

    private fun eventSubscriberMessageOf(
        hash: ByteArray,
        entityKind: PipelineEntityKind = PipelineEntityKind.Transaction()
    ): VersionedEventSubscriptionRequest.V1 {
        return VersionedEventSubscriptionRequest.V1(
            EventSubscriptionRequest(
                Filters.pipeline(entityKind, null, hash)
            )
        )
    }

    private fun readMessage(frame: Frame): EventMessage {
        return when (frame) {
            is Frame.Binary -> {
                when (val versionedMessage = frame.readBytes().let { VersionedEventMessage.decode(it) }) {
                    is VersionedEventMessage.V1 -> versionedMessage.eventMessage
                    else -> throw WebSocketProtocolException(
                        "Expected `${VersionedEventSubscriptionRequest.V1::class.qualifiedName}`, but was `${versionedMessage::class.qualifiedName}`"
                    )
                }
            }

            else -> throw WebSocketProtocolException(
                "Expected server will `${Frame.Binary::class.qualifiedName}` frame, but was `${frame::class.qualifiedName}`"
            )
        }
    }

    /*suspend fun sendTransaction(
        transaction: TransactionBuilder.() -> VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val signedTransaction = transaction(TransactionBuilder())

        val lock = Mutex(locked = true)
        subscribeToTransactionStatus(signedTransaction.hash()) {
            lock.unlock() // 2. unlock after subscription
        }.also {
            lock.lock() // 1. waiting for unlock
            fireAndForget { signedTransaction }
        }
    }*/



}
