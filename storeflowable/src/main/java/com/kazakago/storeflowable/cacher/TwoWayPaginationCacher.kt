package com.kazakago.storeflowable.cacher

public abstract class TwoWayPaginationCacher<PARAM, DATA> : PaginationCacher<PARAM, DATA>() {

    private val prevRequestKeyMap = mutableMapOf<PARAM, String?>()

    public open suspend fun savePrevData(cachedData: List<DATA>, newData: List<DATA>, param: PARAM) {
        saveData(newData + cachedData, param)
    }

    public open suspend fun loadPrevRequestKey(param: PARAM): String? {
        return prevRequestKeyMap[param]
    }

    public open suspend fun savePrevRequestKey(requestKey: String?, param: PARAM) {
        prevRequestKeyMap[param] = requestKey
    }
}
