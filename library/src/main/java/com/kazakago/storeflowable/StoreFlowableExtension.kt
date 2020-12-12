package com.kazakago.storeflowable

fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.createStoreFlowable(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(this)
}
