package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.StoreFlowable

interface PaginatingStoreFlowable<KEY, DATA> : StoreFlowable<KEY, DATA> {

    suspend fun requestAddition(continueWhenError: Boolean = true)
}
