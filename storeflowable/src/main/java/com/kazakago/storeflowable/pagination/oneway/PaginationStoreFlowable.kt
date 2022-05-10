package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.LoadingState

/**
 * Provides input / output methods that abstract the data acquisition destination for one-way pagination.
 *
 * This class is generated from [PaginationStoreFlowableFactory.create].
 *
 * @param DATA Specify the type of data to be handled.
 */
public interface PaginationStoreFlowable<DATA> : StoreFlowable<DATA> {

    /**
     * Request next data.
     *
     * Do nothing if there is no additional data or if already data retrieving.
     *
     * @param continueWhenError Even if the data state is an [LoadingState.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    public suspend fun requestNextData(continueWhenError: Boolean = true)

    /**
     * Treat the passed data as the latest acquired data.
     * and the new data will be notified.
     *
     * Use when new data is created or acquired externally.
     *
     * @param newData Latest data.
     * @param nextKey Key for next request. If null is set, the stored key will be used.
     */
    public suspend fun update(newData: DATA?, nextKey: String? = null)
}
