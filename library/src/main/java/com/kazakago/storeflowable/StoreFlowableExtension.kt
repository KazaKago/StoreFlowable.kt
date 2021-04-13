package com.kazakago.storeflowable

fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(this)
}
