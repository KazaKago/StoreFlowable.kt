package com.kazakago.storeflowable

import com.kazakago.storeflowable.datastate.FlowableDataStateManager

/**
 * Common function of [com.kazakago.storeflowable.StoreFlowableFactory], [com.kazakago.storeflowable.pagination.oneway.OneWayStoreFlowableFactory], [com.kazakago.storeflowable.pagination.twoway.TwoWayStoreFlowableFactory] interfaces.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface BaseStoreFlowableFactory<KEY, DATA> {

    /**
     * Key to which data to get.
     *
     * Please implement so that you can pass the key from the outside.
     */
    val key: KEY

    /**
     * Used for data state management.
     *
     * Create a class that inherits [FlowableDataStateManager] and assign it.
     */
    val flowableDataStateManager: FlowableDataStateManager<KEY>

    /**
     * The data loading process from cache.
     *
     * @return The loaded data.
     */
    suspend fun loadDataFromCache(): DATA?

    /**
     * The data saving process to cache.
     *
     * @param newData Data to be saved.
     */
    suspend fun saveDataToCache(newData: DATA?)

    /**
     * Determine if the cache is valid.
     *
     * @param cachedData Current cache data.
     * @return Returns `true` if the cache is invalid and refresh is needed.
     */
    suspend fun needRefresh(cachedData: DATA): Boolean
}
