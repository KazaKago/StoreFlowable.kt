package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.CacheDataManager

/**
 * Provides functions related to data input / output from cache.
 *
 * @param DATA Specify the type of data to be handled.
 */
interface PaginatingCacheDataManager<DATA> : CacheDataManager<DATA> {

    /**
     * Saves additional data from pagination.
     *
     * Must be combined with existing cached data before saving when implementing this method.
     *
     * @param cachedData existing cache data.
     * @param newData Newly added data.
     */
    suspend fun saveAdditionalDataToCache(cachedData: DATA?, newData: DATA)
}
