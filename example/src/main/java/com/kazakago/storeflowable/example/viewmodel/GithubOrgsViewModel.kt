package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubOrgsViewModel : ViewModel() {

    private val _githubOrgs = MutableStateFlow<List<GithubOrg>>(emptyList())
    val githubOrgs = _githubOrgs.asStateFlow()
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
        githubRepository.refreshOrgs()
        _isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshOrgs()
    }

    fun requestAddition() = viewModelScope.launch {
        githubRepository.requestAdditionalOrgs(continueWhenError = false)
    }

    fun retryAddition() = viewModelScope.launch {
        githubRepository.requestAdditionalOrgs(continueWhenError = true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followOrgs().collect {
            it.doAction(
                onLoading = { githubOrgs ->
                    if (githubOrgs != null) {
                        _githubOrgs.value = githubOrgs
                        _isMainLoading.value = false
                    } else {
                        _githubOrgs.value = emptyList()
                        _isMainLoading.value = true
                    }
                    _isAdditionalLoading.value = false
                    _mainError.value = null
                    _additionalError.value = null
                },
                onCompleted = { githubOrgs, next ->
                    next.doAction(
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
                    _githubOrgs.value = githubOrgs
                    _isMainLoading.value = false
                    _mainError.value = null
                },
                onError = { exception ->
                    _githubOrgs.value = emptyList()
                    _isMainLoading.value = false
                    _isAdditionalLoading.value = false
                    _mainError.value = exception
                    _additionalError.value = null
                },
            )
        }
    }
}
