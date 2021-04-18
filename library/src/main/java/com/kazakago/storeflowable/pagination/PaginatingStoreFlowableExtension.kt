package com.kazakago.storeflowable.pagination

fun <KEY, DATA> PaginatingStoreFlowableCallback<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(this)
}
