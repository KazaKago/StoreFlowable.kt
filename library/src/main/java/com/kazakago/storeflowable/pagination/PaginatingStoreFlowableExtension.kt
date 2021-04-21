package com.kazakago.storeflowable.pagination

/**
 * Create [PaginatingStoreFlowable] class from [PaginatingStoreFlowableCallback].
 */
fun <KEY, DATA> PaginatingStoreFlowableCallback<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(this)
}
