package com.kazakago.cachesample.data.repository.cacheflowable

internal interface CacheDataManager<DATA> {
    suspend fun load(): DATA?
    suspend fun save(data: DATA?)
}
