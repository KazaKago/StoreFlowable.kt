package com.kazakago.storeflowable

fun <KEY, DATA> StoreFlowableCallback<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(this)
}

fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(toStoreFlowableCallback())
}

private fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.toStoreFlowableCallback(): StoreFlowableCallback<KEY, DATA> {
    return object : StoreFlowableCallback<KEY, DATA> {

        override val key = this@toStoreFlowableCallback.key

        override val flowableDataStateManager = this@toStoreFlowableCallback.flowableDataStateManager

        override suspend fun loadData(): DATA? {
            return this@toStoreFlowableCallback.loadData()
        }

        override suspend fun saveData(newData: DATA?) {
            this@toStoreFlowableCallback.saveData(newData)
        }

        override suspend fun fetchOrigin(): FetchingResult<DATA> {
            return FetchingResult(this@toStoreFlowableCallback.fetchOrigin())
        }

        override suspend fun needRefresh(cachedData: DATA): Boolean {
            return this@toStoreFlowableCallback.needRefresh(cachedData)
        }
    }
}

@Deprecated("Use getData()", ReplaceWith("getData(type)"))
suspend inline fun <KEY, DATA> StoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): DATA? {
    return runCatching { get(type) }.getOrNull()
}
