package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    val githubRepos = _githubRepos.asStateFlow()
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading = _isMainLoading.asStateFlow()
    private val _isAdditionalLoading = MutableStateFlow(false)
    val isAdditionalLoading = _isAdditionalLoading.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError = _mainError.asStateFlow()
    private val _additionalError = MutableStateFlow<Exception?>(null)
    val additionalError = _additionalError.asStateFlow()
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

    fun requestAddition() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, continueWhenError = false)
    }

    fun retryAddition() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, continueWhenError = true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followRepos(userName).collect {
            it.doAction(
                onLoading = { githubRepos ->
                    if (githubRepos != null) {
                        _githubRepos.value = githubRepos
                        _isMainLoading.value = false
                    } else {
                        _githubRepos.value = emptyList()
                        _isMainLoading.value = true
                    }
                    _isAdditionalLoading.value = false
                    _mainError.value = null
                    _additionalError.value = null
                },
                onCompleted = { githubRepos, appending ->
                    appending.doAction(
                        onFixed = {
                            _isAdditionalLoading.value = false
                            _additionalError.value = null
                        },
                        onLoading = {
                            _isAdditionalLoading.value = true
                            _additionalError.value = null
                        },
                        onError = { exception ->
                            _isAdditionalLoading.value = false
                            _additionalError.value = exception
                        }
                    )
                    _githubRepos.value = githubRepos
                    _isMainLoading.value = false
                    _mainError.value = null
                },
                onError = { exception ->
                    _githubRepos.value = emptyList()
                    _isMainLoading.value = false
                    _isAdditionalLoading.value = false
                    _mainError.value = exception
                    _additionalError.value = null
                },
            )
        }
    }
}
