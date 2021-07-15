package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.FlowLoadingState
import com.kazakago.storeflowable.core.LoadingState
import kotlinx.coroutines.flow.Flow

/**
 * Provides input / output methods that abstract the data acquisition destination.
 *
 * This class is generated from [StoreFlowableFactory.create].
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface StoreFlowable<KEY, DATA> : BaseStoreFlowable<KEY, DATA> {

    /**
     * Treat the passed data as the latest acquired data.
     * and the new data will be notified.
     *
     * Use when new data is created or acquired externally.
     *
     * @param newData Latest data.
     */
    suspend fun update(newData: DATA?)
}
