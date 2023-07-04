package jp.co.soramitsu.load.base.scenarious

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.parametersOf
import jp.co.soramitsu.iroha2.Page
import jp.co.soramitsu.iroha2.generated.Domain
import jp.co.soramitsu.iroha2.generated.Pagination
import jp.co.soramitsu.iroha2.generated.Sorting
import jp.co.soramitsu.iroha2.generated.VersionedSignedQuery
import jp.co.soramitsu.iroha2.query.QueryAndExtractor

open class SendQuery {

    fun <T> getBody(
        queryAndExtractor: QueryAndExtractor<T>,
        page: Pagination? = null,
        sorting: Sorting? = null,
    ): ByteArray {
        val response = VersionedSignedQuery.encode(queryAndExtractor.query)
        response.let {
            page.also {
                if (it != null) {
                    parametersOf("start", it.start.toString())
                    parametersOf("limit", it.limit.toString())
                }
                sorting.also {
                    if (it != null) {
                        parametersOf("sort_by_metadata_key", it.sortByMetadataKey?.string.toString())
                    }
                }
            }
        }
        return response
    }

    fun convertToByteArray(page: Page<List<Domain>>): ByteArray {
        val objectMapper = ObjectMapper()
        return objectMapper.writeValueAsBytes(page)
    }
}
