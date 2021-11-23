package com.kazakago.storeflowable.pagination.twoway

import com.kazakago.storeflowable.BaseStoreFlowableFactory
import com.kazakago.storeflowable.pagination.oneway.Fetched

/**
 * Abstract factory class for [TwoWayPaginationStoreFlowable] class.
 *
 * Create a class that implements origin or cache data Input / Output / Two-Way-Pagination according to this interface.
 *
 * @param PARAM Specify the type that is the key to retrieve the data. If there is only one data to handle, specify the [Unit] type.
 * @param DATA Specify the type of data to be handled.
 */
interface TwoWayPaginationStoreFlowableFactory<PARAM, DATA> : BaseStoreFlowableFactory<PARAM, DATA> {

    /**
     * The next data saving process to cache.
     * You need to merge cached data & new fetched next data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    suspend fun saveNextDataToCache(cachedData: DATA, newData: DATA, param: PARAM)

    /**
     * The previous data saving process to cache.
     * You need to merge cached data & new fetched previous data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    suspend fun savePrevDataToCache(cachedData: DATA, newData: DATA, param: PARAM)

    /**
     * The latest data acquisition process from origin.
     *
     * @return [Fetched] class including the acquired data.
     */
    suspend fun fetchDataFromOrigin(param: PARAM): FetchedInitial<DATA>

    /**
     * Next data acquisition process from origin.
     *
     * @param nextKey Key for next data request.
     * @return [Fetched] class including the acquired data.
     */
    suspend fun fetchNextDataFromOrigin(nextKey: String, param: PARAM): FetchedNext<DATA>

    /**
     * Previous data acquisition process from origin.
     *
     * @param prevKey Key for previous data request.
     * @return [Fetched] class including the acquired data.
     */
    suspend fun fetchPrevDataFromOrigin(prevKey: String, param: PARAM): FetchedPrev<DATA>
}
