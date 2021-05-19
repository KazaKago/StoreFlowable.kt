package com.kazakago.storeflowable

import com.kazakago.storeflowable.pagination.PaginatingStoreFlowable
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableFactory
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableImpl

/**
 * Create [StoreFlowable] class from [StoreFlowableFactory].
 *
 * @return Created StateFlowable.
 */
fun <KEY, DATA> StoreFlowableFactory<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = this,
        originDataManager = this,
        needRefresh = { needRefresh(it) }
    )
}

/**
 * Create [PaginatingStoreFlowable] class from [PaginatingStoreFlowableFactory].
 *
 * @return Created PaginatingStoreFlowable.
 */
fun <KEY, DATA> PaginatingStoreFlowableFactory<KEY, DATA>.create(): PaginatingStoreFlowable<KEY, DATA> {
    return PaginatingStoreFlowableImpl(
        key = key,
        dataStateManager = flowableDataStateManager,
        cacheDataManager = this,
        originDataManager = this,
        needRefresh = { needRefresh(it) }
    )
}

@Deprecated("Use StoreFlowableFactory.create")
fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return toStoreFlowableFactory().create()
}

private fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.toStoreFlowableFactory(): StoreFlowableFactory<KEY, DATA> {
    return object : StoreFlowableFactory<KEY, DATA> {

        override val key = this@toStoreFlowableFactory.key

        override val flowableDataStateManager = this@toStoreFlowableFactory.flowableDataStateManager

        override suspend fun loadDataFromCache(): DATA? {
            return this@toStoreFlowableFactory.loadData()
        }

        override suspend fun saveDataToCache(newData: DATA?) {
            this@toStoreFlowableFactory.saveData(newData)
        }

        override suspend fun fetchDataFromOrigin(): FetchingResult<DATA> {
            return FetchingResult(this@toStoreFlowableFactory.fetchOrigin())
        }

        override suspend fun needRefresh(cachedData: DATA): Boolean {
            return this@toStoreFlowableFactory.needRefresh(cachedData)
        }
    }
}

/**
 * Deprecated, use [StoreFlowable.getData].
 *
 * @see StoreFlowable.getData
 */
@Deprecated("Use getData()", ReplaceWith("getData(type)"))
suspend inline fun <KEY, DATA> StoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): DATA? {
    return runCatching { get(type) }.getOrNull()
}
