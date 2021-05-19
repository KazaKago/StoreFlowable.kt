package com.kazakago.storeflowable

@Deprecated("Use StoreFlowableFactory", ReplaceWith("StoreFlowableFactory<KEY, DATA>"))
typealias StoreFlowableCallback<KEY, DATA> = StoreFlowableFactory<KEY, DATA>
