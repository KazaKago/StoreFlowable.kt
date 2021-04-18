package com.kazakago.storeflowable

interface CacheDataManager<DATA> {

    suspend fun loadDataFromCache(): DATA?

    suspend fun saveDataToCache(newData: DATA?)
}
