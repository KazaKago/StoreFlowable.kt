package com.kazakago.storeflowable

/**
 * Common function of [com.kazakago.storeflowable.StoreFlowableFactory], [com.kazakago.storeflowable.pagination.oneway.PaginationStoreFlowableFactory], [com.kazakago.storeflowable.pagination.twoway.TwoWayPaginationStoreFlowableFactory] interfaces.
 *
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
@Deprecated("use Cacher class & Fetcher class")
public interface BaseStoreFlowableFactory<PARAM, DATA> {

    /**
     * Used for data state management.
     *
     * Create a class that inherits [FlowableDataStateManager] and assign it.
     */
    public val flowableDataStateManager: FlowableDataStateManager<PARAM>

    /**
     * The data loading process from cache.
     *
     * @return The loaded data.
     */
    public suspend fun loadDataFromCache(param: PARAM): DATA?

    /**
     * The data saving process to cache.
     *
     * @param newData Data to be saved.
     */
    public suspend fun saveDataToCache(newData: DATA?, param: PARAM)

    /**
     * Determine if the cache is valid.
     *
     * @param cachedData Current cache data.
     * @return Returns `true` if the cache is invalid and refresh is needed.
     */
    public suspend fun needRefresh(cachedData: DATA, param: PARAM): Boolean
}
