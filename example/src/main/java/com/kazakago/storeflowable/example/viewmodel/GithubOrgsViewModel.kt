package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.repository.GithubOrgsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GithubOrgsViewModel : ViewModel() {

    private val _orgsStatus = MutableStateFlow(OrgsStatus())
    val orgsStatus = _orgsStatus.asStateFlow()
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading = _isMainLoading.asStateFlow()
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError = _mainError.asStateFlow()
    private val githubOrgsRepository = GithubOrgsRepository()

    data class OrgsStatus(
        var githubOrgs: List<GithubOrg> = emptyList(),
        var isNextLoading: Boolean = false,
        var nextError: Exception? = null,
    )

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        _isRefreshing.value = true
        githubOrgsRepository.refresh()
        _isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubOrgsRepository.refresh()
    }

    fun requestNext() = viewModelScope.launch {
        githubOrgsRepository.requestNext(continueWhenError = false)
    }

    fun retryNext() = viewModelScope.launch {
        githubOrgsRepository.requestNext(continueWhenError = true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubOrgsRepository.follow().collect {
            it.doAction(
                onLoading = { githubOrgs ->
                    if (githubOrgs != null) {
                        _orgsStatus.value = OrgsStatus(githubOrgs = githubOrgs)
                        _isMainLoading.value = false
                    } else {
                        _orgsStatus.value = OrgsStatus(githubOrgs = emptyList())
                        _isMainLoading.value = true
                    }
                    _mainError.value = null
                },
                onCompleted = { githubOrgs, next, _ ->
                    val orgsStatus = OrgsStatus(githubOrgs = githubOrgs)
                    next.doAction(
                        onFixed = {},
                        onLoading = {
                            orgsStatus.isNextLoading = true
                        },
                        onError = { exception ->
                            orgsStatus.nextError = exception
                        }
                    )
                    _orgsStatus.value = orgsStatus
                    _isMainLoading.value = false
                    _mainError.value = null
                },
                onError = { exception ->
                    _orgsStatus.value = OrgsStatus()
                    _isMainLoading.value = false
                    _mainError.value = exception
                },
            )
        }
    }
}
