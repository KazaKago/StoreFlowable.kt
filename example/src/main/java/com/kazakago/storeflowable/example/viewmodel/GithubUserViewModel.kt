package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kazakago.storeflowable.example.model.GithubUser
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubUserViewModel(application: Application, private val userName: String) : AndroidViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GithubUserViewModel(application, userName) as T
        }
    }

    val githubUser = MutableLiveData<GithubUser?>()
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
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
                        onExist = { _githubUser ->
                            githubUser.value = _githubUser
                            isLoading.value = false
                            error.value = null
                        },
                        onNotExist = {
                            githubUser.value = null
                            isLoading.value = false
                            error.value = null
                        }
                    )
                },
                onLoading = {
                    it.content.doAction(
                        onExist = { _githubUser ->
                            githubUser.value = _githubUser
                            isLoading.value = true
                            error.value = null
                        },
                        onNotExist = {
                            githubUser.value = null
                            isLoading.value = true
                            error.value = null
                        }
                    )
                },
                onError = { exception ->
                    it.content.doAction(
                        onExist = { _githubUser ->
                            githubUser.value = _githubUser
                            isLoading.value = false
                            error.value = null
                        },
                        onNotExist = {
                            githubUser.value = null
                            isLoading.value = false
                            error.value = exception
                        }
                    )
                }
            )
        }
    }
}
