package com.kazakago.cachesample.data.repository.dispatcher

import com.kazakago.cachesample.data.cache.state.PagingDataState
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

class PagingCacheStreamDispatcher<ENTITY>(
    private val loadState: (() -> Flow<PagingDataState>),
    private val saveState: (suspend (state: PagingDataState) -> Unit),
    private val loadEntity: (suspend () -> (List<ENTITY>?)),
    private val saveEntity: (suspend (entity: List<ENTITY>?, additionalRequest: Boolean) -> Unit),
    private val fetchOrigin: (suspend (entity: List<ENTITY>?, additionalRequest: Boolean) -> List<ENTITY>),
    private val needRefresh: (suspend (entity: List<ENTITY>) -> Boolean)
) {

    fun subscribe(forceRefresh: Boolean = false): Flow<State<List<ENTITY>>> {
        return loadState()
            .onStart {
                CoroutineScope(Dispatchers.IO).launch { checkState(forceRefresh, clearCache = true, fetchOnError = false, additionalRequest = false) }
            }
            .map {
                mapState(it)
            }
    }

    suspend fun validate() {
        return checkState(forceRefresh = false, clearCache = true, fetchOnError = false, additionalRequest = false)
    }

    suspend fun request() {
        return checkState(forceRefresh = true, clearCache = false, fetchOnError = true, additionalRequest = false)
    }

    suspend fun requestAdditional(fetchOnError: Boolean = true) {
        return checkState(forceRefresh = false, clearCache = false, fetchOnError = fetchOnError, additionalRequest = true)
    }

    suspend fun update(newEntity: List<ENTITY>?, additionalRequest: Boolean = false) {
        val entity = loadEntity()
        val mergedEntity = if (additionalRequest) (entity ?: emptyList()) + (newEntity ?: emptyList()) else (newEntity ?: emptyList())
        saveEntity(mergedEntity, additionalRequest)
        val isReachLast = mergedEntity.isEmpty()
        saveState(PagingDataState.Fixed(isReachLast))
    }

    private suspend fun mapState(dataState: PagingDataState): State<List<ENTITY>> {
        val entity = loadEntity()
        val stateContent: StateContent<List<ENTITY>> = if (entity == null) {
            StateContent.NotExist()
        } else {
            StateContent.Exist(entity)
        }
        return when (dataState) {
            is PagingDataState.Fixed -> State.Fixed(stateContent)
            is PagingDataState.Loading -> State.Loading(stateContent)
            is PagingDataState.Error -> State.Error(stateContent, dataState.exception)
        }
    }

    private suspend fun checkState(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean, additionalRequest: Boolean) {
        val state = loadState().first()
        val entity = loadEntity()
        when (state) {
            is PagingDataState.Fixed -> checkEntity(entity, forceRefresh, clearCache, additionalRequest, state.isReachLast)
            is PagingDataState.Loading -> Unit
            is PagingDataState.Error -> if (fetchOnError) fetchNewEntity(entity, clearCache, additionalRequest)
        }
    }

    private suspend fun checkEntity(entity: List<ENTITY>?, forceRefresh: Boolean, clearCache: Boolean, additionalRequest: Boolean, currentIsReachLast: Boolean) {
        if (entity == null || forceRefresh || needRefresh(entity) || (additionalRequest && !currentIsReachLast)) {
            fetchNewEntity(entity, clearCache, additionalRequest)
        }
    }

    private suspend fun fetchNewEntity(entity: List<ENTITY>?, clearCache: Boolean, additionalRequest: Boolean) {
        try {
            if (clearCache) saveEntity(null, additionalRequest)
            saveState(PagingDataState.Loading)
            val fetchedEntity = fetchOrigin(entity, additionalRequest)
            val mergedEntity = if (additionalRequest) (entity ?: emptyList()) + fetchedEntity else fetchedEntity
            saveEntity(mergedEntity, additionalRequest)
            val isReachLast = fetchedEntity.isEmpty()
            saveState(PagingDataState.Fixed(isReachLast))
        } catch (exception: Exception) {
            saveState(PagingDataState.Error(exception))
        }
    }

}
