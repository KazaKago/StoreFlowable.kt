package com.kazakago.storeflowable.paging

fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.createStoreFlowable(): PagingStoreFlowable<KEY, DATA> {
    return PagingStoreFlowableImpl(this)
}
