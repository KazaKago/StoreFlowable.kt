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
        var isNextLoading: Boolean = false,
        var isPrevLoading: Boolean = false,
        var nextError: Exception? = null,
        var prevError: Exception? = null,
    )

    init {
        subscribe()
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshTwoWayRepos()
    }

    fun requestNext() = viewModelScope.launch {
        githubRepository.requestTwoWayNextRepos(continueWhenError = false)
    }

    fun requestPrev() = viewModelScope.launch {
        githubRepository.requestTwoWayPrevRepos(continueWhenError = false)
    }

    fun retryNext() = viewModelScope.launch {
        githubRepository.requestTwoWayNextRepos(continueWhenError = true)
    }

    fun retryPrev() = viewModelScope.launch {
        githubRepository.requestTwoWayPrevRepos(continueWhenError = true)
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
                onCompleted = { githubRepos, next, prev ->
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
                    prev.doAction(
                        onFixed = {},
                        onLoading = {
                            reposStatus.isPrevLoading = true
                        },
                        onError = { exception ->
                            reposStatus.prevError = exception
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
