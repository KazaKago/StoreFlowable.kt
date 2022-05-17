package com.kazakago.storeflowable.cacher

public abstract class PaginationCacher<PARAM, DATA> : Cacher<PARAM, List<DATA>>() {

    private val nextRequestKeyMap = mutableMapOf<PARAM, String?>()

    public open suspend fun saveNextData(cachedData: List<DATA>, newData: List<DATA>, param: PARAM) {
        saveData(cachedData + newData, param)
    }

    public open suspend fun loadNextRequestKey(param: PARAM): String? {
        return nextRequestKeyMap[param]
    }

    public open suspend fun saveNextRequestKey(requestKey: String?, param: PARAM) {
        nextRequestKeyMap[param] = requestKey
    }
}
