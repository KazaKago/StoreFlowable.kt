package com.kazakago.storeflowable

interface OriginDataManager<DATA> {

    suspend fun fetchOrigin(): DATA
}
