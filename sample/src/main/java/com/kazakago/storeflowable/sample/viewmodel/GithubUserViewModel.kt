package com.kazakago.storeflowable.sample.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kazakago.storeflowable.sample.model.GithubUser
import com.kazakago.storeflowable.sample.repository.GithubRepository
import com.kazakago.storeflowable.sample.viewmodel.livedata.MutableLiveEvent
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
    val isRefreshing = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
    val refreshingError = MutableLiveEvent<Exception>()
    private val githubRepository = GithubRepository()

    init {
        subscribe()
    }

    fun request() = viewModelScope.launch {
        isRefreshing.value = true
        githubRepository.requestUser(userName)
        isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubRepository.requestUser(userName)
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
                    if (isRefreshing.value == true) refreshingError.call(exception)
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
