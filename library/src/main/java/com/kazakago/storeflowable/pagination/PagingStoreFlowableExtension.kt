package com.kazakago.storeflowable.pagination

fun <KEY, DATA> PaginatingStoreFlowableResponder<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(this)
}
