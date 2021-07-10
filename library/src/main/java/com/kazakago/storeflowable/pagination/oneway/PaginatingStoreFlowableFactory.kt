package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.BaseStoreFlowableFactory
import com.kazakago.storeflowable.pagination.FetchingResult

/**
 * Abstract factory class for [PaginatingStoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output according to this interface.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface PaginatingStoreFlowableFactory<KEY, DATA> : BaseStoreFlowableFactory<KEY, DATA> {

    /**
     * TODO
     */
    suspend fun saveAppendingDataToCache(cachedData: DATA?, newData: DATA)

    /**
     * The latest data acquisition process from origin.
     *
     * @return [FetchingResult] class including the acquired data.
     */
    suspend fun fetchDataFromOrigin(): FetchingResult<DATA>

    /**
     * TODO
     */
    suspend fun fetchAppendingDataFromOrigin(cachedData: DATA?): FetchingResult<DATA>
}
