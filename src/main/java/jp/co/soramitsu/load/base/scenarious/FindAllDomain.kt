package jp.co.soramitsu.load.base.scenarious

import io.gatling.http.response.ByteArrayResponseBody
import io.gatling.http.response.Response
import io.gatling.javaapi.core.CoreDsl.ByteArrayBody
import io.gatling.javaapi.core.CoreDsl.bodyString
import io.gatling.javaapi.core.CoreDsl.scenario
import io.gatling.javaapi.core.ScenarioBuilder
import io.gatling.javaapi.http.HttpDsl.http
import jp.co.soramitsu.iroha2.asDomainId
import jp.co.soramitsu.iroha2.asName
import jp.co.soramitsu.iroha2.generated.Domain
import jp.co.soramitsu.iroha2.generated.AccountId
import jp.co.soramitsu.iroha2.generated.VersionedPaginatedQueryResult
import jp.co.soramitsu.iroha2.keyPairFromHex
import jp.co.soramitsu.iroha2.query.QueryAndExtractor
import jp.co.soramitsu.iroha2.query.QueryBuilder
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets

class FindAllDomain : SendQuery() {
    val peerUrl = "http://127.0.0.1:8080"
    val telemetryUrl = "http://127.0.0.1:8180"
    val admin = AccountId("alice".asName(), "wonderland".asDomainId())
    val adminKeyPair = keyPairFromHex(
        "7233bfc89dcbd68c19fde6ce6158225298ec1131b6a130d1aeb454c1ab5183c0",
        "9ac47abf59b356e0bd7dcbbbb4dec080e302156a48ca907e47cb6aea1d32719e",
    )
    val queryAndExtractor: QueryAndExtractor<List<Domain>> =
        QueryBuilder.findAllDomains().account(admin).buildSigned(adminKeyPair)

    companion object {
        @JvmStatic
        fun apply() = runBlocking { FindAllDomain().applyScn() }
    }

    val scn = scenario("Kotlin scn")
        .exec(
            http("get all domain")
                .post(peerUrl + "/query")
                .header("Content-Type", "application/parity-scale-codec")
                .body(ByteArrayBody(getBody(queryAndExtractor)))
                .transformResponse { Response, Session ->
                    if (Response.status().code() == 200) {
                        return@transformResponse Response(
                            Response.request(),
                            Response.startTimestamp(),
                            Response.endTimestamp(),
                            Response.status(),
                            Response.headers(),
                            ByteArrayResponseBody(
                                Response.body().bytes()
                                    .let { VersionedPaginatedQueryResult.decode(Response.body().bytes()) }
                                    .let { queryAndExtractor.resultExtractor.extract(it) }
                                    .let { convertToByteArray(it) },
                                StandardCharsets.UTF_8,
                            ),
                            Response.checksums(),
                            Response.isHttp2,
                        )
                    } else {
                        return@transformResponse Response
                    }
                }
                .check(bodyString().saveAs("responseBody")),
        ).exec { Session ->
            val responseBody = Session.get<String>("responseBody")
            println("Response Body: $responseBody")
            Session
        }

    fun applyScn(): ScenarioBuilder {
        return scn
    }
}
