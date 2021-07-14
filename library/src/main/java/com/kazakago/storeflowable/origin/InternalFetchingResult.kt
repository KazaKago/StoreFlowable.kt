package com.kazakago.storeflowable.origin

internal data class InternalFetchingResult<DATA>(
    val data: DATA,
    val nextKey: String?,
    val prevKey: String?,
)
