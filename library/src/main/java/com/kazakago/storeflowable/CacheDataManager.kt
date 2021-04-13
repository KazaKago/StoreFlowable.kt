package com.kazakago.storeflowable

interface CacheDataManager<DATA> {

    suspend fun loadData(): DATA?

    suspend fun saveData(newData: DATA?)
}
