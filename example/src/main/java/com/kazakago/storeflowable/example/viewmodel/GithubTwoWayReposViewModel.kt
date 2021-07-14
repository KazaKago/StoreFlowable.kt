package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubTwoWayReposViewModel : ViewModel() {

    private val _reposStatus = MutableStateFlow(ReposStatus())
    val reposStatus = _reposStatus.asStateFlow()
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading = _isMainLoading.asStateFlow()
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError = _mainError.asStateFlow()
    private val githubRepository = GithubRepository()

    data class ReposStatus(
        var githubRepos: List<GithubRepo> = emptyList(),
        var isAppendingLoading: Boolean = false,
        var isPrependingLoading: Boolean = false,
        var appendingError: Exception? = null,
        var prependingError: Exception? = null,
    )

    init {
        subscribe()
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshTwoWayRepos()
    }

    fun requestAppending() = viewModelScope.launch {
        githubRepository.requestAppendingTwoWayRepos(continueWhenError = false)
    }

    fun requestPrepending() = viewModelScope.launch {
        githubRepository.requestPrependingTwoWayRepos(continueWhenError = false)
    }

    fun retryAppending() = viewModelScope.launch {
        githubRepository.requestAppendingTwoWayRepos(continueWhenError = true)
    }

    fun retryPrepending() = viewModelScope.launch {
        githubRepository.requestPrependingTwoWayRepos(continueWhenError = true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followTwoWayRepos().collect {
            it.doAction(
                onLoading = { githubRepos ->
                    if (githubRepos != null) {
                        _reposStatus.value = ReposStatus(githubRepos = githubRepos)
                        _isMainLoading.value = false
                    } else {
                        _reposStatus.value = ReposStatus(githubRepos = emptyList())
                        _isMainLoading.value = true
                    }
                    _mainError.value = null
                },
                onCompleted = { githubRepos, appending, prepending ->
                    val reposStatus = ReposStatus(githubRepos = githubRepos)
                    appending.doAction(
                        onFixed = {},
                        onLoading = {
                            reposStatus.isAppendingLoading = true
                        },
                        onError = { exception ->
                            reposStatus.appendingError = exception
                        }
                    )
                    prepending.doAction(
                        onFixed = {},
                        onLoading = {
                            reposStatus.isPrependingLoading = true
                        },
                        onError = { exception ->
                            reposStatus.prependingError = exception
                        }
                    )
                    _reposStatus.value = reposStatus
                    _isMainLoading.value = false
                    _mainError.value = null
                },
                onError = { exception ->
                    _reposStatus.value = ReposStatus()
                    _isMainLoading.value = false
                    _mainError.value = exception
                },
            )
        }
    }
}
