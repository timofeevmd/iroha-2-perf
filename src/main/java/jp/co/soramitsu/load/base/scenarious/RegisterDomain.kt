package jp.co.soramitsu.load.base.scenarious

import io.gatling.javaapi.core.CoreDsl
import io.gatling.javaapi.core.CoreDsl.exec
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.core.Session
import io.gatling.javaapi.http.HttpDsl
import io.gatling.javaapi.http.HttpDsl.ws
import io.ktor.http.websocket.*
import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.VersionedSignedTransaction
import jp.co.soramitsu.iroha2.transaction.TransactionBuilder
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.time.withTimeout
import java.net.URL
import java.time.Duration

open class RegisterDomain : SendTransaction() {

    val admin = AccountId("bob".asName(), "wonderland".asDomainId())
    val adminKeyPair = keyPairFromHex(
        "7233bfc89dcbd68c19fde6ce6158225298ec1131b6a130d1aeb454c1ab5183c0",
        "9ac47abf59b356e0bd7dcbbbb4dec080e302156a48ca907e47cb6aea1d32719e",
    )
    val newDomainId = "looking_glass_${System.currentTimeMillis()}_new".asDomainId()
    val transaction: VersionedSignedTransaction =
        TransactionBuilder().account(admin).registerDomain(newDomainId).buildSigned(adminKeyPair)

    val client = AdminIroha2Client(URL(peerUrl), log = true)

    companion object {
        @JvmStatic
        fun apply() = runBlocking { RegisterDomain().applyScn() }
    }

    val scn = scenario("RegisterDomain")
        /*.exec(
            HttpDsl.http("Register domain")
                .post(peerUrl + "/transaction")
                .header("Content-Type", "application/parity-scale-codec")
                .body(CoreDsl.ByteArrayBody(sendNewTransaction(client, transaction).getCompleted())),
            )*/
        .exec { session: Session ->
            runBlocking {
                client.sendTransaction {
                    account(admin)
                    registerDomain(newDomainId)
                    buildSigned(adminKeyPair)
                }
            }
            //CoreDsl.ByteArrayBody(sendNewTransaction(client, transaction).getCompleted())
            session
        }

    fun applyScn(): ScenarioBuilder? {
        return scn
    }
}
