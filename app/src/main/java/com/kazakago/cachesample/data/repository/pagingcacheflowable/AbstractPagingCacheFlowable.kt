package com.kazakago.cachesample.data.repository.pagingcacheflowable

import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.data.repository.cacheflowable.DataStateManager
import com.kazakago.cachesample.data.repository.cacheflowable.FlowAccessor
import com.kazakago.cachesample.data.repository.cacheflowable.FlowableDataStateManager
import kotlinx.coroutines.flow.Flow

internal abstract class AbstractPagingCacheFlowable<KEY, DATA>(key: KEY) : PagingCacheFlowable<KEY, DATA>(key) {

    protected abstract val flowableDataStateManager: FlowableDataStateManager<KEY>

    protected abstract suspend fun loadData(): List<DATA>?

    protected abstract suspend fun saveData(data: List<DATA>?, additionalRequest: Boolean)

    protected abstract suspend fun needRefresh(data: List<DATA>): Boolean

    protected abstract suspend fun fetchOrigin(data: List<DATA>?, additionalRequest: Boolean): List<DATA>

    override val flowAccessor: FlowAccessor<KEY> = object : FlowAccessor<KEY> {
        override fun getFlow(key: KEY): Flow<DataState> = flowableDataStateManager.getFlow(key)
    }

    override val dataSelector: PagingDataSelector<KEY, DATA> = PagingDataSelector(
        key = key,
        dataStateManager = object : DataStateManager<KEY> {
            override fun save(key: KEY, state: DataState) = flowableDataStateManager.save(key, state)
            override fun load(key: KEY): DataState = flowableDataStateManager.load(key)
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
