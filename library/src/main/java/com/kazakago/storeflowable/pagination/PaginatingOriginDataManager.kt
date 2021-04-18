package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.OriginDataManager

interface PaginatingOriginDataManager<DATA> : OriginDataManager<DATA> {

    suspend fun fetchAdditionalDataFromOrigin(cachedData: DATA?): FetchingResult<DATA>
}
