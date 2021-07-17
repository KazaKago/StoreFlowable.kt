package com.kazakago.storeflowable.origin

internal data class InternalFetched<DATA>(
    val data: DATA,
    val nextKey: String?,
    val prevKey: String?,
)
