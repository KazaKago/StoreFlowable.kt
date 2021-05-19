package com.kazakago.storeflowable.pagination

@Deprecated("Use PaginatingStoreFlowableFactory", ReplaceWith("PaginatingStoreFlowableFactory<KEY, DATA>"))
typealias PaginatingStoreFlowableCallback<KEY, DATA> = PaginatingStoreFlowableFactory<KEY, DATA>
