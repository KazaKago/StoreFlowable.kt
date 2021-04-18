package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType
import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowable

@Deprecated("Use PaginatingStoreFlowable from PaginatingStoreFlowableCallback.create()")
internal class PagingStoreFlowableImpl<KEY, DATA>(private val paginatingStoreFlowable: PaginatingStoreFlowable<KEY, List<DATA>>) : PagingStoreFlowable<KEY, DATA> {

    override fun publish(forceRefresh: Boolean): FlowableState<List<DATA>> {
        return paginatingStoreFlowable.publish(forceRefresh)
    }

    override suspend fun getData(type: AsDataType): List<DATA>? {
        return paginatingStoreFlowable.getData(type)
    }

    override suspend fun requireData(type: AsDataType): List<DATA> {
        return paginatingStoreFlowable.requireData(type)
    }

    override suspend fun validate() {
        return paginatingStoreFlowable.validate()
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        paginatingStoreFlowable.refresh(clearCacheWhenFetchFails, continueWhenError)
    }

    override suspend fun requestAddition(continueWhenError: Boolean) {
        paginatingStoreFlowable.requestAddition(continueWhenError)
    }

    override suspend fun update(newData: List<DATA>?) {
        paginatingStoreFlowable.update(newData)
    }
}
