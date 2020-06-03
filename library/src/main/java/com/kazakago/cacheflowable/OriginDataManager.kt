package com.kazakago.cacheflowable

interface OriginDataManager<DATA> {
    suspend fun fetch(): DATA
}