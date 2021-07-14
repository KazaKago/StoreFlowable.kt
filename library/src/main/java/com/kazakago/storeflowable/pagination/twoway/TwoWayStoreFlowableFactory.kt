package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.BaseStoreFlowableFactory
import com.kazakago.storeflowable.pagination.oneway.FetchingResult

/**
 * Abstract factory class for [TwoWayStoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output / Two-Way-Pagination according to this interface.
 *
 * @param KEY Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface TwoWayStoreFlowableFactory<KEY, DATA> : BaseStoreFlowableFactory<KEY, DATA> {

    /**
     * The next data saving process to cache.
     * You need to merge cached data & new fetched next data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    suspend fun saveNextDataToCache(cachedData: DATA?, newData: DATA)

    /**
     * The previous data saving process to cache.
     * You need to merge cached data & new fetched previous data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    suspend fun savePrevDataToCache(cachedData: DATA?, newData: DATA)

    /**
     * The latest data acquisition process from origin.
     *
     * @return [FetchingResult] class including the acquired data.
     */
    suspend fun fetchDataFromOrigin(): FetchingTwoWayResult<DATA>

    /**
     * Next data acquisition process from origin.
     *
     * @return [FetchingResult] class including the acquired data.
     */
    suspend fun fetchNextDataFromOrigin(nextKey: String): FetchingNextResult<DATA>

    /**
     * Previous data acquisition process from origin.
     *
     * @return [FetchingResult] class including the acquired data.
     */
    suspend fun fetchPrevDataFromOrigin(prevKey: String): FetchingPrevResult<DATA>
}
