package jp.co.soramitsu.load.base.scenarious

import com.fasterxml.jackson.databind.module.SimpleModule
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.utils.*
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration

open class SendTransaction {

    val peerUrl = "http://127.0.0.1:8080"
    val telemetryUrl = "http://127.0.0.1:8180"

    companion object {
        @JvmStatic
        fun sendNewTransaction(client: AdminIroha2Client, transaction: VersionedSignedTransaction) =
            runBlocking { SendTransaction().sendTransaction(client, transaction)
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

    suspend fun sendTransaction(
        client: AdminIroha2Client,
        transaction: VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val lock = Mutex(locked = true)

        client.subscribeToTransactionStatus(transaction.hash())
            .also {
                lock.unlock()
                lock.lock()
                fireAndForget { transaction }
            }
    }
}
