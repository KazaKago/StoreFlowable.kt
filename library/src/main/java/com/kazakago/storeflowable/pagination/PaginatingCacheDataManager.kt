package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.CacheDataManager

interface PaginatingCacheDataManager<DATA> : CacheDataManager<DATA> {

    suspend fun saveAdditionalDataToCache(cachedData: DATA?, newData: DATA)
}
