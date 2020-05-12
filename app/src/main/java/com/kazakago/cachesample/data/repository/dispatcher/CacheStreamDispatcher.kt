package com.kazakago.cachesample.data.repository.dispatcher

import com.kazakago.cachesample.data.cache.DataStateCache
import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.data.cache.state.getOrCreate
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

abstract class CacheStreamDispatcher<ENTITY>(private val dataId: String) {

    protected abstract suspend fun loadEntity(): ENTITY?

    protected abstract suspend fun saveEntity(entity: ENTITY?)

    protected abstract suspend fun fetchOrigin(): ENTITY

    protected abstract suspend fun needRefresh(entity: ENTITY): Boolean

    protected open fun loadDataStateFlow(): Flow<DataState> {
        return DataStateCache.dataState.getOrCreate(dataId)
    }

    protected open suspend fun saveDataState(state: DataState) {
        DataStateCache.dataState.getOrCreate(dataId).value = state
    }

    fun getFlow(forceRefresh: Boolean = false): Flow<State<ENTITY>> {
        return loadDataStateFlow()
            .onStart {
                CoroutineScope(Dispatchers.IO).launch { separateState(forceRefresh, clearCache = true, fetchOnError = false) }
            }
            .map {
                mapState(it)
            }
    }

    suspend fun validate() {
        return separateState(forceRefresh = false, clearCache = true, fetchOnError = false)
    }

    suspend fun request() {
        return separateState(forceRefresh = true, clearCache = false, fetchOnError = true)
    }

    suspend fun update(newEntity: ENTITY?) {
        saveEntity(newEntity)
        saveDataState(DataState.Fixed)
    }

    private suspend fun mapState(dataState: DataState): State<ENTITY> {
        val entity = loadEntity()
        val stateContent = if (entity == null) {
            StateContent.NotExist<ENTITY>()
        } else {
            StateContent.Exist(entity)
        }
        return when (dataState) {
            is DataState.Fixed -> State.Fixed(stateContent)
            is DataState.Loading -> State.Loading(stateContent)
            is DataState.Error -> State.Error(stateContent, dataState.exception)
        }
    }

    private suspend fun separateState(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean) {
        when (loadDataStateFlow().first()) {
            is DataState.Fixed -> separateEntity(forceRefresh, clearCache)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchOnError) fetchNewEntity(clearCache)
        }
    }

    private suspend fun separateEntity(forceRefresh: Boolean, clearCache: Boolean) {
        val entity = loadEntity()
        if (entity == null || forceRefresh || needRefresh(entity)) {
            fetchNewEntity(clearCache)
        }
    }

    private suspend fun fetchNewEntity(clearCache: Boolean) {
        try {
            if (clearCache) saveEntity(null)
            saveDataState(DataState.Loading)
            val fetchedEntity = fetchOrigin()
            saveEntity(fetchedEntity)
            saveDataState(DataState.Fixed)
        } catch (exception: Exception) {
            saveDataState(DataState.Error(exception))
        }
    }

}
