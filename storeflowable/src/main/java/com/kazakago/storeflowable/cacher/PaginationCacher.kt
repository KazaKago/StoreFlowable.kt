package com.kazakago.storeflowable.cacher

/**
 * A Cacher class that supports pagination in one direction.
 * Override the get / set methods as needed.
 *
 * @see com.kazakago.storeflowable.from
 */
public abstract class PaginationCacher<PARAM, DATA> : Cacher<PARAM, List<DATA>>() {

    private val nextRequestKeyMap = mutableMapOf<PARAM, String?>()

    /**
     * The next data saving process to cache.
     * You need to merge cached data & new fetched next data.
     *
     * @param cachedData Currently cached data.
     * @param newData Data to be saved.
     * @param param Key to get the specified data.
     */
    public open suspend fun saveNextData(cachedData: List<DATA>, newData: List<DATA>, param: PARAM) {
        saveData(cachedData + newData, param)
    }

    /**
     * Get RequestKey to Fetch the next pagination data.
     *
     * @param param Key to get the specified data.
     */
    public open suspend fun loadNextRequestKey(param: PARAM): String? {
        return nextRequestKeyMap[param]
    }

    /**
     * Save RequestKey to Fetch the next pagination data.
     *
     * @param requestKey pagination request key.
     * @param param Key to get the specified data.
     */
    public open suspend fun saveNextRequestKey(requestKey: String?, param: PARAM) {
        nextRequestKeyMap[param] = requestKey
    }
}
