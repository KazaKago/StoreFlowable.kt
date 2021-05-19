package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.GettingFrom
import com.kazakago.storeflowable.core.FlowableState
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowable

@Deprecated("Use PaginatingStoreFlowable from PaginatingStoreFlowableFactory.create()")
internal class PagingStoreFlowableImpl<KEY, DATA>(private val paginatingStoreFlowable: PaginatingStoreFlowable<KEY, List<DATA>>) : PagingStoreFlowable<KEY, DATA> {

    override fun publish(forceRefresh: Boolean): FlowableState<List<DATA>> {
        return paginatingStoreFlowable.publish(forceRefresh)
    }

    override suspend fun getData(from: GettingFrom): List<DATA>? {
        return paginatingStoreFlowable.getData(from)
    }

    override suspend fun requireData(from: GettingFrom): List<DATA> {
        return paginatingStoreFlowable.requireData(from)
    }

    override suspend fun validate() {
        return paginatingStoreFlowable.validate()
    }

    override suspend fun refresh(clearCacheWhenFetchFails: Boolean, continueWhenError: Boolean) {
        paginatingStoreFlowable.refresh(clearCacheWhenFetchFails, continueWhenError)
    }

    override suspend fun requestAdditionalData(continueWhenError: Boolean) {
        paginatingStoreFlowable.requestAdditionalData(continueWhenError)
    }

    override suspend fun update(newData: List<DATA>?) {
        paginatingStoreFlowable.update(newData)
    }
}
