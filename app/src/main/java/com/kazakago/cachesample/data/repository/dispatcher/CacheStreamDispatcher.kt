package com.kazakago.cachesample.data.repository.dispatcher

import com.kazakago.cachesample.data.cache.DataState
import com.kazakago.cachesample.domain.model.State
import com.kazakago.cachesample.domain.model.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class CacheStreamDispatcher<ENTITY>(
    private val loadState: (() -> Flow<DataState>),
    private val saveState: ((state: DataState) -> Unit),
    private val loadEntity: (suspend () -> (ENTITY?)),
    private val saveEntity: (suspend (entity: ENTITY?) -> Unit),
    private val fetchOrigin: (suspend () -> (ENTITY)),
    private val needRefresh: (suspend (entity: ENTITY) -> Boolean)
) {

    fun subscribe(forceRefresh: Boolean = false): Flow<State<ENTITY>> {
        return loadState()
            .onStart {
                CoroutineScope(Dispatchers.IO).launch { checkState(forceRefresh, clearCache = true, fetchOnError = false) }
            }
            .map {
                mapState(it)
            }
    }

    suspend fun validate() {
        return checkState(forceRefresh = false, clearCache = true, fetchOnError = false)
    }

    suspend fun request() {
        return checkState(forceRefresh = true, clearCache = false, fetchOnError = true)
    }

    suspend fun update(newEntity: ENTITY?) {
        saveEntity(newEntity)
        saveState(DataState.Fixed)
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

    private suspend fun checkState(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean) {
        when (loadState().first()) {
            is DataState.Fixed -> checkEntity(forceRefresh, clearCache)
            is DataState.Loading -> Unit
            is DataState.Error -> if (fetchOnError) fetchNewEntity(clearCache)
        }
    }

    private suspend fun checkEntity(forceRefresh: Boolean, clearCache: Boolean) {
        val entity = loadEntity()
        if (entity == null || forceRefresh || needRefresh(entity)) {
            fetchNewEntity(clearCache)
        }
    }

    private suspend fun fetchNewEntity(clearCache: Boolean) {
        try {
            if (clearCache) saveEntity(null)
            saveState(DataState.Loading)
            val fetchedEntity = fetchOrigin()
            saveEntity(fetchedEntity)
            saveState(DataState.Fixed)
        } catch (exception: Exception) {
            saveState(DataState.Error(exception))
        }
    }

}
