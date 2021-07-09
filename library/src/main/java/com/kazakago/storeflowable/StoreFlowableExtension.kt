package com.kazakago.storeflowable

import com.kazakago.storeflowable.pagination.PaginatingStoreFlowable
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableFactory
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableImpl

/**
 * Create [StoreFlowable] class from [StoreFlowableFactory].
 *
 * @return Created StateFlowable.
 */
fun <KEY, DATA> StoreFlowableFactory<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = this,
        originDataManager = this,
        needRefresh = { needRefresh(it) }
    )
}

/**
 * Create [PaginatingStoreFlowable] class from [PaginatingStoreFlowableFactory].
 *
 * @return Created PaginatingStoreFlowable.
 */
fun <KEY, DATA> PaginatingStoreFlowableFactory<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(
        key = key,
        flowableDataStateManager = flowableDataStateManager,
        cacheDataManager = this,
        originDataManager = this,
        needRefresh = { needRefresh(it) }
    )
}
