package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.StoreFlowable
import com.kazakago.storeflowable.core.State

/**
 * Provides input / output methods that abstract the data acquisition destination.
 *
 * This class is generated from [PaginatingStoreFlowableFactory.create].
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface PaginatingStoreFlowable<KEY, DATA> : StoreFlowable<KEY, DATA> {

    /**
     * Request appending data.
     *
     * Do nothing if there is no additional data or if already data retrieving.
     *
     * @param continueWhenError Even if the data state is an [State.Error] when [refresh] is called, the refresh will continue. Default value is `true`.
     */
    suspend fun requestAppendingData(continueWhenError: Boolean = true)
}
