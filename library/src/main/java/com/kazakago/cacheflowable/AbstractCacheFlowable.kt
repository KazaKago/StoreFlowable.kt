package com.kazakago.cacheflowable

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow

abstract class AbstractCacheFlowable<KEY, DATA>(key: KEY) : CacheFlowable<KEY, DATA>(key) {

    protected abstract val flowableDataStateManager: FlowableDataStateManager<KEY>

    protected abstract suspend fun loadData(): DATA?

    protected abstract suspend fun saveData(data: DATA?)

    protected abstract suspend fun needRefresh(data: DATA): Boolean

    protected abstract suspend fun fetchOrigin(): DATA

    @ExperimentalCoroutinesApi
    override val flowAccessor: FlowAccessor<KEY> = object : FlowAccessor<KEY> {
        override fun getFlow(key: KEY): Flow<DataState> = flowableDataStateManager.getFlow(key)
    }

    @ExperimentalCoroutinesApi
    override val dataSelector: DataSelector<KEY, DATA> = DataSelector(
        key = key,
        dataStateManager = object : DataStateManager<KEY> {
            override fun save(key: KEY, state: DataState) = flowableDataStateManager.save(key, state)
            override fun load(key: KEY): DataState = flowableDataStateManager.load(key)
        },
        cacheDataManager = object : CacheDataManager<DATA> {
            override suspend fun load(): DATA? = loadData()
            override suspend fun save(data: DATA?) = saveData(data)
        },
        originDataManager = object : OriginDataManager<DATA> {
            override suspend fun fetch(): DATA = this@AbstractCacheFlowable.fetchOrigin()
        },
        needRefresh = {
            needRefresh(it)
        }
    )

}
