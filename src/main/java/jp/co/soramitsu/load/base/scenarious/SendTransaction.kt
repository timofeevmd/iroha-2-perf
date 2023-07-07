package jp.co.soramitsu.load.base.scenarious

import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.generated.*
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.time.withTimeout
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
