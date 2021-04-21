package com.kazakago.storeflowable

/**
 * Callback class used from [StoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output according to this interface.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface StoreFlowableCallback<KEY, DATA> : CacheDataManager<DATA>, OriginDataManager<DATA> {

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
     * Determine if the cache is valid.
     *
     * @param cachedData Current cache data.
     * @return Returns `true` if the cache is invalid and refresh is needed.
     */
    suspend fun needRefresh(cachedData: DATA): Boolean
}
