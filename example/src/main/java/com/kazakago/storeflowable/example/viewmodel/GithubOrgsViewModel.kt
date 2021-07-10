package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubOrgsViewModel(application: Application) : AndroidViewModel(application) {

    private val _githubOrgs = MutableStateFlow<List<GithubOrg>>(emptyList())
    val githubOrgs: StateFlow<List<GithubOrg>> get() = _githubOrgs
    private val _isMainLoading = MutableStateFlow(false)
    val isMainLoading: StateFlow<Boolean> = _isMainLoading
    private val _isAdditionalLoading = MutableStateFlow(false)
    val isAdditionalLoading: StateFlow<Boolean> = _isAdditionalLoading
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> get() = _isRefreshing
    private val _mainError = MutableStateFlow<Exception?>(null)
    val mainError: StateFlow<Exception?> get() = _mainError
    private val _additionalError = MutableStateFlow<Exception?>(null)
    val additionalError: StateFlow<Exception?> get() = _additionalError
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

    fun requestAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalOrgs(false)
    }

    fun retryAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalOrgs(true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followOrgs().collect {
            it.doAction(
                onLoading = { githubOrgs ->
                    if (githubOrgs != null) {
                        _githubOrgs.value = githubOrgs
                        _isMainLoading.value = false
                        _isAdditionalLoading.value = false
                        _mainError.value = null
                        _additionalError.value = null
                    } else {
                        _githubOrgs.value = emptyList()
                        _isMainLoading.value = true
                        _isAdditionalLoading.value = false
                        _mainError.value = null
                        _additionalError.value = null
                    }
                },
                onCompleted = { githubOrgs, appending, _ ->
                    appending.doAction(
                        onFixed = {
                            _githubOrgs.value = githubOrgs
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = null
                        },
                        onLoading = {
                            _githubOrgs.value = githubOrgs
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = true
                            _mainError.value = null
                            _additionalError.value = null
                        },
                        onError = { exception ->
                            _githubOrgs.value = githubOrgs
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = exception
                        }
                    )
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
