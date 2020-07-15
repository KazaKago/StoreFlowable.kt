package com.kazakago.cacheflowable

internal interface CacheDataManager<DATA> {
    suspend fun load(): DATA?
    suspend fun save(data: DATA?)
}
