package com.kazakago.storeflowable.paging

import com.kazakago.storeflowable.AsDataType

suspend inline fun <KEY, DATA> PagingStoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): List<DATA>? {
    return runCatching { get(type) }.getOrNull()
}

fun <KEY, DATA> PagingStoreFlowableResponder<KEY, DATA>.createStoreFlowable(): PagingStoreFlowable<KEY, DATA> {
    return PagingStoreFlowableImpl(this)
}
