package com.kazakago.storeflowable

/**
 * Provides functions related to data input / output from cache.
 *
 * @param DATA Specify the type of data to be handled.
 */
interface CacheDataManager<DATA> {

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
}
