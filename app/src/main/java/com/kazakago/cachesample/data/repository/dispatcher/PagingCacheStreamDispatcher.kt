package com.kazakago.cachesample.data.repository.dispatcher

import com.kazakago.cachesample.data.cache.state.PagingDataState
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

abstract class PagingCacheStreamDispatcher<ENTITY> {

    protected abstract fun loadDataStateFlow(): StateFlow<PagingDataState>

    protected abstract suspend fun saveDataState(state: PagingDataState)

    protected abstract suspend fun loadEntity(): List<ENTITY>?

    protected abstract suspend fun saveEntity(entity: List<ENTITY>?, additionalRequest: Boolean)

    protected abstract suspend fun fetchOrigin(entity: List<ENTITY>?, additionalRequest: Boolean): List<ENTITY>

    protected abstract suspend fun needRefresh(entity: List<ENTITY>): Boolean

    fun getFlow(forceRefresh: Boolean = false): Flow<State<List<ENTITY>>> {
        return loadDataStateFlow()
            .onStart {
                CoroutineScope(Dispatchers.IO).launch { separateState(forceRefresh, clearCache = true, fetchOnError = false, additionalRequest = false) }
            }
            .map {
                mapState(it)
            }
    }

    suspend fun validate() {
        return separateState(forceRefresh = false, clearCache = true, fetchOnError = false, additionalRequest = false)
    }

    suspend fun request() {
        return separateState(forceRefresh = true, clearCache = false, fetchOnError = true, additionalRequest = false)
    }

    suspend fun requestAdditional(fetchOnError: Boolean = true) {
        return separateState(forceRefresh = false, clearCache = false, fetchOnError = fetchOnError, additionalRequest = true)
    }

    suspend fun update(newEntity: List<ENTITY>?, additionalRequest: Boolean = false) {
        val entity = loadEntity()
        val mergedEntity = if (additionalRequest) (entity ?: emptyList()) + (newEntity ?: emptyList()) else (newEntity ?: emptyList())
        saveEntity(mergedEntity, additionalRequest)
        val isReachLast = mergedEntity.isEmpty()
        saveDataState(PagingDataState.Fixed(isReachLast))
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

    private suspend fun separateState(forceRefresh: Boolean, clearCache: Boolean, fetchOnError: Boolean, additionalRequest: Boolean) {
        val state = loadDataStateFlow().value
        val entity = loadEntity()
        when (state) {
            is PagingDataState.Fixed -> separateEntity(entity, forceRefresh, clearCache, additionalRequest, state.isReachLast)
            is PagingDataState.Loading -> Unit
            is PagingDataState.Error -> if (fetchOnError) fetchNewEntity(entity, clearCache, additionalRequest)
        }
    }

    private suspend fun separateEntity(entity: List<ENTITY>?, forceRefresh: Boolean, clearCache: Boolean, additionalRequest: Boolean, currentIsReachLast: Boolean) {
        if (entity == null || forceRefresh || needRefresh(entity) || (additionalRequest && !currentIsReachLast)) {
            fetchNewEntity(entity, clearCache, additionalRequest)
        }
    }

    private suspend fun fetchNewEntity(entity: List<ENTITY>?, clearCache: Boolean, additionalRequest: Boolean) {
        try {
            if (clearCache) saveEntity(null, additionalRequest)
            saveDataState(PagingDataState.Loading)
            val fetchedEntity = fetchOrigin(entity, additionalRequest)
            val mergedEntity = if (additionalRequest) (entity ?: emptyList()) + fetchedEntity else fetchedEntity
            saveEntity(mergedEntity, additionalRequest)
            val isReachLast = fetchedEntity.isEmpty()
            saveDataState(PagingDataState.Fixed(isReachLast))
        } catch (exception: Exception) {
            saveDataState(PagingDataState.Error(exception))
        }
    }

}
