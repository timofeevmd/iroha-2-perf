package jp.co.soramitsu.load.base.scenarious;

import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.generated.datamodel.account.AccountId
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain
import jp.co.soramitsu.iroha2.generated.datamodel.predicate.GenericValuePredicateBox
import jp.co.soramitsu.iroha2.generated.datamodel.predicate.value.ValuePredicate
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.net.URL
import java.util.concurrent.CompletableFuture


internal open class Constant() {
    val peerUrl = "http://127.0.0.1:8080"
    val telemetryUrl = "http://127.0.0.1:8180"
    val ADMIN = AccountId("bob".asName(), "wonderland".asDomainId())
    val adminKeyPair = keyPairFromHex("7233bfc89dcbd68c19fde6ce6158225298ec1131b6a130d1aeb454c1ab5183c0",
        "9ac47abf59b356e0bd7dcbbbb4dec080e302156a48ca907e47cb6aea1d32719e")

    val client = AdminIroha2Client(URL(peerUrl), URL(telemetryUrl), log = true)


    companion object {
        @JvmStatic
        fun findAllDomainObj() = GlobalScope.launch {
            Constant().newSendQuery()
        }
    }

    suspend fun newSendQuery() =
    client.sendQuery(QueryBuilder.findAllDomains()
        .account(ADMIN)
        .buildSigned(adminKeyPair))

    suspend fun findAllDomains(queryFilter: GenericValuePredicateBox<ValuePredicate>? = null) = QueryBuilder
        .findAllDomains(queryFilter)
        .account(ADMIN)
        .buildSigned(adminKeyPair)
        .let { client.sendQuery(it) }


}
