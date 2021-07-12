package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubMetaViewModel(application: Application) : AndroidViewModel(application) {

    private val _githubMeta = MutableStateFlow<GithubMeta?>(null)
    val githubMeta = _githubMeta.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<Exception?>(null)
    val error = _error.asStateFlow()
    private val githubRepository = GithubRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        githubRepository.refreshMeta()
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshMeta()
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followMeta().collect {
            it.doAction(
                onLoading = {
                    _githubMeta.value = null
                    _isLoading.value = true
                    _error.value = null
                },
                onCompleted = { githubMeta ->
                    _githubMeta.value = githubMeta
                    _isLoading.value = false
                    _error.value = null
                },
                onError = { exception ->
                    _githubMeta.value = null
                    _isLoading.value = false
                    _error.value = exception
                }
            )
        }
    }
}
