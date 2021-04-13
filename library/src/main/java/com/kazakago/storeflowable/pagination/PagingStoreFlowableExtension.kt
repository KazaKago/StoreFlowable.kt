package com.kazakago.storeflowable.pagination

import com.kazakago.storeflowable.AsDataType

suspend inline fun <KEY, DATA> PaginatingStoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): DATA? {
    return runCatching { get(type) }.getOrNull()
}

fun <KEY, DATA> PaginatingStoreFlowableResponder<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(this)
}
