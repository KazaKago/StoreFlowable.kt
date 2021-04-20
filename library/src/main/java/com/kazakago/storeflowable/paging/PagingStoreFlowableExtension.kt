package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableCallback
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableImpl

fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.create(): PagingStoreFlowable<KEY, DATA> {
    return PagingStoreFlowableImpl(PaginatingStoreFlowableImpl(toPaginatingStoreFlowableCallback()))
}

private fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.toPaginatingStoreFlowableCallback(): PaginatingStoreFlowableCallback<KEY, List<DATA>> {
    return object : PaginatingStoreFlowableCallback<KEY, List<DATA>> {

        override val key = this@toPaginatingStoreFlowableCallback.key

        override val flowableDataStateManager = this@toPaginatingStoreFlowableCallback.flowableDataStateManager

        override suspend fun loadDataFromCache(): List<DATA>? {
            return this@toPaginatingStoreFlowableCallback.loadData()
        }

        override suspend fun saveDataToCache(newData: List<DATA>?) {
            this@toPaginatingStoreFlowableCallback.saveData(newData, additionalRequest = false)
        }

        override suspend fun saveAdditionalDataToCache(cachedData: List<DATA>?, newData: List<DATA>) {
            this@toPaginatingStoreFlowableCallback.saveData((cachedData ?: emptyList()) + newData, additionalRequest = true)
        }

        override suspend fun fetchDataFromOrigin(): FetchingResult<List<DATA>> {
            val fetchedData = this@toPaginatingStoreFlowableCallback.fetchOrigin(null, additionalRequest = false)
            return FetchingResult(fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
        }

        override suspend fun fetchAdditionalDataFromOrigin(cachedData: List<DATA>?): FetchingResult<List<DATA>> {
            val fetchedData = this@toPaginatingStoreFlowableCallback.fetchOrigin(cachedData, additionalRequest = true)
            return FetchingResult(fetchedData, noMoreAdditionalData = fetchedData.isEmpty())
        }

        override suspend fun needRefresh(cachedData: List<DATA>): Boolean {
            return this@toPaginatingStoreFlowableCallback.needRefresh(cachedData)
        }
    }
}

@Deprecated("Use getData()", ReplaceWith("getData(type)"))
suspend inline fun <KEY, DATA> PagingStoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): List<DATA>? {
    return runCatching { get(type) }.getOrNull()
}