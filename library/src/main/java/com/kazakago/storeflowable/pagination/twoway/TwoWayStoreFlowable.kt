package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.BaseStoreFlowable
import com.kazakago.storeflowable.core.LoadingState
import com.kazakago.storeflowable.core.pagination.twoway.FlowableTwoWayLoadingState
import kotlinx.coroutines.flow.Flow

/**
 * Provides input / output methods that abstract the data acquisition destination for two-way pagination.
 *
 * This class is generated from [TwoWayStoreFlowableFactory.create].
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface TwoWayStoreFlowable<KEY, DATA> : BaseStoreFlowable<KEY, DATA> {

    /**
     * Returns a [FlowableTwoWayLoadingState] that can continuously receive changes in the state of the data.
     *
     * If the data has not been acquired yet, new data will be automatically acquired when this [Flow] is collected.
     *
     * The error when retrieving data is included in [LoadingState.Error].
     * and this method itself does not throw an [Exception].
     *
     * @param forceRefresh Set to `true` if you want to forcibly retrieve data from origin when collecting. Default value is `false`.
     * @return Returns a [Flow] containing the state of the data.
     */
    fun publish(forceRefresh: Boolean = false): FlowableTwoWayLoadingState<DATA>

    /**
     * Request appending data.
     *
     * Do nothing if there is no additional data or if already data retrieving.
     *
     * @param continueWhenError Even if the data state is an [LoadingState.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    suspend fun requestAppendingData(continueWhenError: Boolean = true)

    /**
     * Request prepending data.
     *
     * Do nothing if there is no additional data or if already data retrieving.
     *
     * @param continueWhenError Even if the data state is an [LoadingState.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    suspend fun requestPrependingData(continueWhenError: Boolean = true)
}
