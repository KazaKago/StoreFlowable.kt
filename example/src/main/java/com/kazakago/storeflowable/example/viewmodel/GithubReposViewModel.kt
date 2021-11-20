package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.repository.GithubReposRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubReposViewModel(private val userName: String) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GithubReposViewModel(userName) as T
        }
    }

    private val _reposStatus = MutableStateFlow(ReposStatus())
    val reposStatus = _reposStatus.asStateFlow()
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading = _isMainLoading.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError = _mainError.asStateFlow()
    private val githubReposRepository = GithubReposRepository()

    data class ReposStatus(
        var githubRepos: List<GithubRepo> = emptyList(),
        var isNextLoading: Boolean = false,
        var nextError: Exception? = null,
    )

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        githubReposRepository.refresh(userName)
        _isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubReposRepository.refresh(userName)
    }

    fun requestNext() = viewModelScope.launch {
        githubReposRepository.requestNext(userName, continueWhenError = false)
    }

    fun retryNext() = viewModelScope.launch {
        githubReposRepository.requestNext(userName, continueWhenError = true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubReposRepository.follow(userName).collect {
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
                onCompleted = { githubRepos, next, _ ->
                    val reposStatus = ReposStatus(githubRepos = githubRepos)
                    next.doAction(
                        onFixed = {},
                        onLoading = {
                            reposStatus.isNextLoading = true
                        },
                        onError = { exception ->
                            reposStatus.nextError = exception
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
