package com.kazakago.cacheflowable

interface CacheDataManager<DATA> {
    suspend fun load(): DATA?
    suspend fun save(data: DATA?)
}
