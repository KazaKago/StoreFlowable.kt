package com.kazakago.storeflowable

suspend inline fun <KEY, DATA> StoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): DATA? {
    return runCatching { get(type) }.getOrNull()
}

fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.createStoreFlowable(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(this)
}
