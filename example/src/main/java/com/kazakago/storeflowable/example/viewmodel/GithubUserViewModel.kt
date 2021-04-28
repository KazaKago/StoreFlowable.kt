package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubUserViewModel(application: Application, private val userName: String) : AndroidViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GithubUserViewModel(application, userName) as T
        }
    }

    private val _githubUser = MutableStateFlow<GithubUser?>(null)
    val githubUser: StateFlow<GithubUser?> get() = _githubUser
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading
    private val _error = MutableStateFlow<Exception?>(null)
    val error: StateFlow<Exception?> get() = _error
    private val githubRepository = GithubRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        githubRepository.refreshUser(userName)
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshUser(userName)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followUser(userName).collect {
            it.doAction(
                onFixed = {
                    it.content.doAction(
                        onExist = { githubUser ->
                            _githubUser.value = githubUser
                            _isLoading.value = false
                            _error.value = null
                        },
                        onNotExist = {
                            _githubUser.value = null
                            _isLoading.value = false
                            _error.value = null
                        }
                    )
                },
                onLoading = {
                    it.content.doAction(
                        onExist = { githubUser ->
                            _githubUser.value = githubUser
                            _isLoading.value = true
                            _error.value = null
                        },
                        onNotExist = {
                            _githubUser.value = null
                            _isLoading.value = true
                            _error.value = null
                        }
                    )
                },
                onError = { exception ->
                    it.content.doAction(
                        onExist = { githubUser ->
                            _githubUser.value = githubUser
                            _isLoading.value = false
                            _error.value = null
                        },
                        onNotExist = {
                            _githubUser.value = null
                            _isLoading.value = false
                            _error.value = exception
                        }
                    )
                }
            )
        }
    }
}
