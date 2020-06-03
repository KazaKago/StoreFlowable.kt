package com.kazakago.cachesample.data.repository.cacheflowable

internal interface OriginDataManager<DATA> {
    suspend fun fetch(): DATA
}