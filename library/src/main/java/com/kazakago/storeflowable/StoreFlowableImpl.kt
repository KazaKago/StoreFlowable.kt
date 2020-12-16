package com.kazakago.storeflowable

import com.kazakago.storeflowable.core.State
import com.kazakago.storeflowable.core.StateContent
import kotlinx.coroutines.flow.*

internal class StoreFlowableImpl<KEY, DATA>(private val storeFlowableResponder: StoreFlowableResponder<KEY, DATA>) : StoreFlowable<KEY, DATA> {

    private val dataSelector = DataSelector(
        key = storeFlowableResponder.key,
        dataStateManager = storeFlowableResponder.flowableDataStateManager,
        cacheDataManager = storeFlowableResponder,
        originDataManager = storeFlowableResponder,
        needRefresh = { storeFlowableResponder.needRefresh(it) }
    )

    override fun asFlow(forceRefresh: Boolean): Flow<State<DATA>> {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                dataSelector.doStateAction(forceRefresh = forceRefresh, clearCache = true, fetchAtError = false, fetchAsync = true)
            }
            .map {
                val data = dataSelector.load()
                val stateContent = StateContent.wrap(data)
                it.mapState(stateContent)
            }
    }

    override suspend fun get(type: AsDataType): DATA {
        return storeFlowableResponder.flowableDataStateManager.getFlow(storeFlowableResponder.key)
            .onStart {
                when (type) {
                    AsDataType.Mix -> dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false)
                    AsDataType.FromOrigin -> dataSelector.doStateAction(forceRefresh = true, clearCache = true, fetchAtError = false, fetchAsync = false)
                    AsDataType.FromCache -> Unit //do nothing.
                }
            }
            .transform {
                val data = dataSelector.load()
                when (it) {
                    is DataState.Fixed -> if (data != null) emit(data) else throw NoSuchElementException()
                    is DataState.Loading -> Unit //do nothing.
                    is DataState.Error -> if (data != null) emit(data) else throw it.exception
                }
            }
            .first()
    }

    override suspend fun validate() {
        dataSelector.doStateAction(forceRefresh = false, clearCache = true, fetchAtError = false, fetchAsync = false)
    }

    override suspend fun request() {
        dataSelector.doStateAction(forceRefresh = true, clearCache = false, fetchAtError = true, fetchAsync = false)
    }

    override suspend fun update(newData: DATA?) {
        dataSelector.update(newData)
    }
}
