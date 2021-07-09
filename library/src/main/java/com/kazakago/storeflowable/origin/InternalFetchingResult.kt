package com.kazakago.storeflowable.origin

internal data class InternalFetchingResult<DATA>(
    val data: DATA,
    val noMoreAppendingData: Boolean,
    val noMorePrependingData: Boolean,
)
