package jp.co.soramitsu.load.base.scenarious

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.http.HttpDsl
import io.ktor.http.websocket.*
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.client.Iroha2Client
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.VersionedSignedTransaction
import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import kotlinx.coroutines.runBlocking
import java.net.URL

open class RegisterDomain : SendTransaction() {

    val admin = AccountId("bob".asName(), "wonderland".asDomainId())
    val adminKeyPair = keyPairFromHex(
        "7233bfc89dcbd68c19fde6ce6158225298ec1131b6a130d1aeb454c1ab5183c0",
        "9ac47abf59b356e0bd7dcbbbb4dec080e302156a48ca907e47cb6aea1d32719e",
    )
    val newDomainId = "looking_glass_${System.currentTimeMillis()}".asDomainId()
    val transaction: VersionedSignedTransaction =
        TransactionBuilder().account(admin).registerDomain(newDomainId).buildSigned(adminKeyPair)


    val client = Iroha2Client(URL(peerUrl), log = true)

    companion object {
        @JvmStatic
        fun apply() = runBlocking { RegisterDomain().applyScn() }
    }

    val scn = scenario("RegisterDomain")
        .exec(
            HttpDsl.http("Register domain")
                .post(peerUrl + "/transaction")
                .header("Content-Type", "application/parity-scale-codec")
                //.body(CoreDsl.ByteArrayBody(sendNewTransaction(client, transaction).getCompleted())),
                .body(CoreDsl.ByteArrayBody(sendNewTransaction(client, transaction).getCompleted()))
            )


    fun applyScn(): ScenarioBuilder? {
        return scn
    }
}
