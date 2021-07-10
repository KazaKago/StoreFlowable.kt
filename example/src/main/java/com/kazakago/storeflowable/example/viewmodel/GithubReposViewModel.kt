package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.core.pagination.oneway.AdditionalState
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubReposViewModel(application: Application, private val userName: String) : AndroidViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GithubReposViewModel(application, userName) as T
        }
    }

    private val _githubRepos = MutableStateFlow<List<GithubRepo>>(emptyList())
    val githubRepos: StateFlow<List<GithubRepo>> get() = _githubRepos
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading: StateFlow<Boolean> get() = _isMainLoading
    private val _isAdditionalLoading = MutableStateFlow(false)
    val isAdditionalLoading: StateFlow<Boolean> get() = _isAdditionalLoading
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError: StateFlow<Exception?> get() = _mainError
    private val _additionalError = MutableStateFlow<Exception?>(null)
    val additionalError: StateFlow<Exception?> = _additionalError
    private val githubRepository = GithubRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        githubRepository.refreshRepos(userName)
        _isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshRepos(userName)
    }

    fun requestAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, false)
    }

    fun retryAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followRepos(userName).collect {
            it.doAction(
                onLoading = {
                    _githubRepos.value = emptyList()
                    _isMainLoading.value = true
                    _isAdditionalLoading.value = false
                    _mainError.value = null
                    _additionalError.value = null
                },
                onRefreshing = { githubRepos ->
                    _githubRepos.value = githubRepos
                    _isMainLoading.value = false
                    _isAdditionalLoading.value = false
                    _mainError.value = null
                    _additionalError.value = null
                },
                onCompleted = { githubRepos ->
                    _githubRepos.value = githubRepos
                    _isMainLoading.value = false
                    _isAdditionalLoading.value = false
                    _mainError.value = null
                    _additionalError.value = null
                },
                onError = { exception ->
                    _githubRepos.value = emptyList()
                    _isMainLoading.value = false
                    _isAdditionalLoading.value = false
                    _mainError.value = exception
                    _additionalError.value = null
                },
                onAddition = { githubRepos, appending ->
                    when (appending) {
                        is AdditionalState.Loading -> {
                            _githubRepos.value = githubRepos
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = true
                            _mainError.value = null
                            _additionalError.value = null
                        }
                        is AdditionalState.Error -> {
                            _githubRepos.value = githubRepos
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = appending.exception
                        }
                    }
                }
            )
        }
    }
}
