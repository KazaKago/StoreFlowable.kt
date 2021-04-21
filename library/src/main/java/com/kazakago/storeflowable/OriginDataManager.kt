package com.kazakago.storeflowable

/**
 * Provides functions related to data input / output from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
interface OriginDataManager<DATA> {

    /**
     * The latest data acquisition process from origin.
     *
     * @return [FetchingResult] class including the acquired data
     */
    suspend fun fetchDataFromOrigin(): FetchingResult<DATA>
}
