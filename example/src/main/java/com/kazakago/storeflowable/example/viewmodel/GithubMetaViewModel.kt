package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.repository.GithubMetaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GithubMetaViewModel : ViewModel() {

    private val _githubMeta = MutableStateFlow<GithubMeta?>(null)
    val githubMeta = _githubMeta.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<Exception?>(null)
    val error = _error.asStateFlow()
    private val githubMetaRepository = GithubMetaRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        githubMetaRepository.refresh()
    }

    fun retry() = viewModelScope.launch {
        githubMetaRepository.refresh()
    }

    private fun subscribe() = viewModelScope.launch {
        githubMetaRepository.follow().collect {
            it.doAction(
                onLoading = {
                    _githubMeta.value = null
                    _isLoading.value = true
                    _error.value = null
                },
                onCompleted = { githubMeta, _, _ ->
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
