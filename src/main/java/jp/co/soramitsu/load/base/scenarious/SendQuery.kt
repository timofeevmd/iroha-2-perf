package jp.co.soramitsu.load.base.scenarious;

import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.http.*
import jp.co.soramitsu.iroha2.*
import jp.co.soramitsu.iroha2.generated.datamodel.domain.Domain
import jp.co.soramitsu.iroha2.generated.datamodel.pagination.Pagination
import jp.co.soramitsu.iroha2.generated.datamodel.query.VersionedSignedQueryRequest
import jp.co.soramitsu.iroha2.generated.datamodel.sorting.Sorting
import jp.co.soramitsu.iroha2.query.QueryAndExtractor


open class SendQuery {

    fun <T> getBody(
        queryAndExtractor: QueryAndExtractor<T>,
        page: Pagination? = null,
        sorting: Sorting? = null
    ): ByteArray {
        val response = VersionedSignedQueryRequest.encode(queryAndExtractor.query)
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






