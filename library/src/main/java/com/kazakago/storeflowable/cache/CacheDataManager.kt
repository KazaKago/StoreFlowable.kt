package com.kazakago.storeflowable.cache

internal interface CacheDataManager<DATA> {

    suspend fun load(): DATA?

    suspend fun save(newData: DATA?)

    suspend fun saveAppending(cachedData: DATA?, newData: DATA)

    suspend fun savePrepending(cachedData: DATA?, newData: DATA)
}
