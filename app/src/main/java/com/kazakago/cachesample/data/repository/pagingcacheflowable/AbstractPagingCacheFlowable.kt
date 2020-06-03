package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.PagingDataState
import kotlinx.coroutines.flow.Flow

internal abstract class AbstractPagingCacheFlowable<KEY, DATA>(key: KEY) : PagingCacheFlowable<KEY, DATA>(key) {

    protected abstract val flowableDataStateManager: PagingFlowableDataStateManager<KEY>

    protected abstract suspend fun loadData(): List<DATA>?

    protected abstract suspend fun saveData(data: List<DATA>?, additionalRequest: Boolean)

    protected abstract suspend fun needRefresh(data: List<DATA>): Boolean

    protected abstract suspend fun fetchOrigin(data: List<DATA>?, additionalRequest: Boolean): List<DATA>

    override val flowAccessor: PagingFlowAccessor<KEY> = object : PagingFlowAccessor<KEY> {
        override fun getFlow(key: KEY): Flow<PagingDataState> = flowableDataStateManager.getFlow(key)
    }

    override val dataSelector: PagingDataSelector<KEY, DATA> = PagingDataSelector(
        key = key,
        dataStateManager = object : PagingDataStateManager<KEY> {
            override fun save(key: KEY, state: PagingDataState) = flowableDataStateManager.save(key, state)
            override fun load(key: KEY): PagingDataState = flowableDataStateManager.load(key)
        },
        cacheDataManager = object : PagingCacheDataManager<DATA> {
            override suspend fun load(): List<DATA>? = loadData()
            override suspend fun save(data: List<DATA>?, additionalRequest: Boolean) = saveData(data, additionalRequest)
        },
        originDataManager = object : PagingOriginDataManager<DATA> {
            override suspend fun fetch(data: List<DATA>?, additionalRequest: Boolean): List<DATA> = this@AbstractPagingCacheFlowable.fetchOrigin(data, additionalRequest)
        },
        needRefresh = {
            needRefresh(it)
        }
    )

}
