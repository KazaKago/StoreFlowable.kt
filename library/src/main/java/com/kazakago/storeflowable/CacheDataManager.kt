package com.kazakago.storeflowable

internal interface CacheDataManager<DATA> {
    suspend fun load(): DATA?
    suspend fun save(data: DATA?)
}
