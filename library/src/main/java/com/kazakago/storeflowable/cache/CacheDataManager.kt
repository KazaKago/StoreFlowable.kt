package com.kazakago.storeflowable.cache

internal interface CacheDataManager<DATA> {

    suspend fun load(): DATA?

    suspend fun save(newData: DATA?)

    suspend fun saveNext(cachedData: DATA, newData: DATA)

    suspend fun savePrev(cachedData: DATA, newData: DATA)
}
