package com.kazakago.storeflowable.pagination

/**
 * Create [PaginatingStoreFlowable] class from [PaginatingStoreFlowableCallback].
 *
 * @return Created PaginatingStoreFlowable.
 */
fun <KEY, DATA> PaginatingStoreFlowableCallback<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(this)
}
