package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.pagination.oneway.PaginatingStoreFlowable

/**
 * Provides input / output methods that abstract the data acquisition destination.
 *
 * This class is generated from [TwoWayPaginatingStoreFlowableFactory.create].
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface TwoWayPaginatingStoreFlowable<KEY, DATA> : PaginatingStoreFlowable<KEY, DATA> {

    /**
     * Request prepending data.
     *
     * Do nothing if there is no additional data or if already data retrieving.
     *
     * @param continueWhenError Even if the data state is an [LoadingState.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    suspend fun requestPrependingData(continueWhenError: Boolean = true)
}
