package com.kazakago.storeflowable

internal interface OriginDataManager<DATA> {
    suspend fun fetch(): DATA
}