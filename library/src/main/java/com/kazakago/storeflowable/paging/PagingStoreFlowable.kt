package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.pagination.PaginatingStoreFlowable

@Deprecated("Use PaginatingStoreFlowable from PaginatingStoreFlowableCallback.create()")
interface PagingStoreFlowable<KEY, DATA> : PaginatingStoreFlowable<KEY, List<DATA>> {

    @Deprecated("Use requestAddition", ReplaceWith("requestAddition(continueWhenError)"))
    suspend fun requestAdditional(continueWhenError: Boolean = true) = requestAdditionalData(continueWhenError)
}
