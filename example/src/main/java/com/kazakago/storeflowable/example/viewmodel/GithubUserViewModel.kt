package com.kazakago.storeflowable.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.example.repository.GithubUserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GithubUserViewModel(private val userName: String) : ViewModel() {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GithubUserViewModel(userName) as T
        }
    }

    private val _githubUser = MutableStateFlow<GithubUser?>(null)
    val githubUser = _githubUser.asStateFlow()
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    private val _error = MutableStateFlow<Exception?>(null)
    val error = _error.asStateFlow()
    private val githubUserRepository = GithubUserRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        githubUserRepository.refresh(userName)
    }

    fun retry() = viewModelScope.launch {
        githubUserRepository.refresh(userName)
    }

    private fun subscribe() = viewModelScope.launch {
        githubUserRepository.follow(userName).collect {
            it.doAction(
                onLoading = {
                    _githubUser.value = null
                    _isLoading.value = true
                    _error.value = null
                },
                onCompleted = { githubUser, _, _ ->
                    _githubUser.value = githubUser
                    _isLoading.value = false
                    _error.value = null
                },
                onError = { exception ->
                    _githubUser.value = null
                    _isLoading.value = false
                    _error.value = exception
                }
            )
        }
    }
}
