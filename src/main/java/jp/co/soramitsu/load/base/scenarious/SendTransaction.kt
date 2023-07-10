package jp.co.soramitsu.load.base.scenarious

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.client.Iroha2Client
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
        @JvmStatic
        fun fireAndForgetNew (client: AdminIroha2Client, transaction: VersionedSignedTransaction) =
            runBlocking { SendTransaction().fireAndForget(client) {transaction} }
    }

    open val logger: Logger = LoggerFactory.getLogger(javaClass)

    suspend fun fireAndForget(client: Iroha2Client, transaction: TransactionBuilder.() -> VersionedSignedTransaction): ByteArray {
        val signedTransaction = transaction(TransactionBuilder.builder())
        val hash = signedTransaction.hash()
        logger.debug("Sending transaction with hash {}", hash.toHex())
        val request: ByteArray = VersionedSignedTransaction.encode(signedTransaction)
        return request
        /*val response: HttpResponse = client.post("$peerUrl${Iroha2Client.TRANSACTION_ENDPOINT}") {
            setBody(VersionedSignedTransaction.encode(signedTransaction))
        }
        response.body<Unit>()
        return hash*/
    }

    /*suspend fun sendTransaction(
        client: Iroha2Client,
        transaction: VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val lock = Mutex(locked = true)

        client.subscribeToTransactionStatus(transaction.hash())
            .also {
                lock.unlock()
                lock.lock()
                fireAndForget (client){ transaction }
            }
    }*/
    suspend fun sendTransaction(
        client: Iroha2Client,
        transaction: VersionedSignedTransaction
    ): CompletableDeferred<ByteArray> = coroutineScope {
        val lock = Mutex(locked = true)

        client.subscribeToTransactionStatus(transaction.hash())
            .also {
                lock.unlock()
                lock.lock()
            }
    }


}
