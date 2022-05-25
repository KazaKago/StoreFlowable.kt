package com.kazakago.storeflowable.pagination.oneway

import com.kazakago.storeflowable.BaseStoreFlowableFactory

/**
 * Abstract factory class for [PaginationStoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output / One-Way-Pagination according to this interface.
 *
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
@Deprecated("use Cacher class & Fetcher class")
public interface PaginationStoreFlowableFactory<PARAM, DATA> : BaseStoreFlowableFactory<PARAM, DATA> {

    /**
     * The next data saving process to cache.
     * You need to merge cached data & new fetched next data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    public suspend fun saveNextDataToCache(cachedData: DATA, newData: DATA, param: PARAM)

    /**
     * The latest data acquisition process from origin.
     *
     * @return [Fetched] class including the acquired data.
     */
    public suspend fun fetchDataFromOrigin(param: PARAM): Fetched<DATA>

    /**
     * Next data acquisition process from origin.
     *
     * @param nextKey Key for next data request.
     * @return [Fetched] class including the acquired data.
     */
    public suspend fun fetchNextDataFromOrigin(nextKey: String, param: PARAM): Fetched<DATA>
}
