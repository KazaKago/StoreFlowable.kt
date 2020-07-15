package com.kazakago.cacheflowable

internal interface OriginDataManager<DATA> {
    suspend fun fetch(): DATA
}