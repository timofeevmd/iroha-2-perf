package jp.co.soramitsu.load.base.scenarious

import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import jp.co.soramitsu.iroha2.AdminIroha2Client
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.datamodel.transaction.VersionedSignedTransaction
import jp.co.soramitsu.iroha2.hash
import jp.co.soramitsu.iroha2.toHex
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.net.URL

open class SendTransaction {

    val peerUrl = "http://127.0.0.1:8080"
    val telemetryUrl = "http://127.0.0.1:8180"

    companion object {
        @JvmStatic
        fun sendNewTransaction(transaction: VersionedSignedTransaction) = runBlocking { SendTransaction().sendTransaction(transaction) }
    }

    open val logger: Logger = LoggerFactory.getLogger(javaClass)
    val client = Iroha2Client(URL(peerUrl), log = true)

    fun fireAndForget(transaction: TransactionBuilder.() -> VersionedSignedTransaction): ByteArray {
        val signedTransaction = transaction(TransactionBuilder.builder())
        val hash = signedTransaction.hash()
        logger.debug("Sending transaction with hash {}", hash.toHex())
        val response: ByteArray = VersionedSignedTransaction.encode(signedTransaction)
        return response
    }

    suspend fun sendTransaction(
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

