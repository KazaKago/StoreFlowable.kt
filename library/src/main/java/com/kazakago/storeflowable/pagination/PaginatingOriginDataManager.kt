package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.OriginDataManager

/**
 * Provides functions related to data input / output from origin.
 *
 * @param DATA Specify the type of data to be handled.
 */
interface PaginatingOriginDataManager<DATA> : OriginDataManager<DATA> {

    /**
     * The additional data acquisition process from origin.
     *
     * Get from origin considering pagination when implementing this method.
     *
     * @param cachedData existing cache data.
     * @return [FetchingResult] class including the acquired data
     */
    suspend fun fetchAdditionalDataFromOrigin(cachedData: DATA?): FetchingResult<DATA>
}
