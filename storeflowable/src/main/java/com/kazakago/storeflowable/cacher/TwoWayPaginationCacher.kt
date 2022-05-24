package com.kazakago.storeflowable.cacher

/**
 * A Cacher class that supports pagination in two direction.
 * Override the get / set methods as needed.
 *
 * @see com.kazakago.storeflowable.from
 */
public abstract class TwoWayPaginationCacher<PARAM, DATA> : PaginationCacher<PARAM, DATA>() {

    private val prevRequestKeyMap = mutableMapOf<PARAM, String?>()

    /**
     * The previous data saving process to cache.
     * You need to merge cached data & new fetched previous data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     */
    public open suspend fun savePrevData(cachedData: List<DATA>, newData: List<DATA>, param: PARAM) {
        saveData(newData + cachedData, param)
    }

    /**
     * Get RequestKey to Fetch the prev pagination data.
     *
     * @param param Key to get the specified data.
     */
    public open suspend fun loadPrevRequestKey(param: PARAM): String? {
        return prevRequestKeyMap[param]
    }

    /**
     * Save RequestKey to Fetch the prev pagination data.
     *
     * @param requestKey pagination request key.
     * @param param Key to get the specified data.
     */
    public open suspend fun savePrevRequestKey(requestKey: String?, param: PARAM) {
        prevRequestKeyMap[param] = requestKey
    }
}
