package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.create
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableFactory

@Deprecated("Use PaginatingStoreFlowableFactory.create")
fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.create(): PagingStoreFlowable<KEY, DATA> {
    return PagingStoreFlowableImpl(toPaginatingStoreFlowableFactory().create())
}

private fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.toPaginatingStoreFlowableFactory(): PaginatingStoreFlowableFactory<KEY, List<DATA>> {
    return object : PaginatingStoreFlowableFactory<KEY, List<DATA>> {

        override val key = this@toPaginatingStoreFlowableFactory.key

        override val flowableDataStateManager = this@toPaginatingStoreFlowableFactory.flowableDataStateManager

        override suspend fun loadDataFromCache(): List<DATA>? {
            return this@toPaginatingStoreFlowableFactory.loadData()
        }

        override suspend fun saveDataToCache(newData: List<DATA>?) {
            this@toPaginatingStoreFlowableFactory.saveData(newData, additionalRequest = false)
        }

        override suspend fun saveAdditionalDataToCache(cachedData: List<DATA>?, newData: List<DATA>) {
            this@toPaginatingStoreFlowableFactory.saveData((cachedData ?: emptyList()) + newData, additionalRequest = true)
        }

        override suspend fun fetchDataFromOrigin(): FetchingResult<List<DATA>> {
            val fetchedData = this@toPaginatingStoreFlowableFactory.fetchOrigin(null, additionalRequest = false)
            return FetchingResult(fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
        }

        override suspend fun fetchAdditionalDataFromOrigin(cachedData: List<DATA>?): FetchingResult<List<DATA>> {
            val fetchedData = this@toPaginatingStoreFlowableFactory.fetchOrigin(cachedData, additionalRequest = true)
            return FetchingResult(fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
        }

        override suspend fun needRefresh(cachedData: List<DATA>): Boolean {
            return this@toPaginatingStoreFlowableFactory.needRefresh(cachedData)
        }
    }
}

@Deprecated("Use getData()", ReplaceWith("getData(type)"))
suspend inline fun <KEY, DATA> PagingStoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): List<DATA>? {
    return runCatching { get(type) }.getOrNull()
}
