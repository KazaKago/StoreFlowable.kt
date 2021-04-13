package com.kazakago.storeflowable

data class FetchingResult<DATA>(
    val data: DATA,
    val noMoreAdditionalData: Boolean = false,
)
