package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubReposViewModel(application: Application, private val userName: String) : AndroidViewModel(application) {

    @Suppress("UNCHECKED_CAST")
    class Factory(private val application: Application, private val userName: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return GithubReposViewModel(application, userName) as T
        }
    }

    val githubRepos = MutableLiveData<List<GithubRepo>>(emptyList())
    val isMainLoading = MutableLiveData(false)
    val isAdditionalLoading = MutableLiveData(false)
    val isRefreshing = MutableLiveData(false)
    val mainError = MutableLiveData<Exception?>()
    val additionalError = MutableLiveData<Exception?>()
    private val githubRepository = GithubRepository()

    init {
        subscribe()
    }

    fun refresh() = viewModelScope.launch {
        isRefreshing.value = true
        githubRepository.refreshRepos(userName)
        isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshRepos(userName)
    }

    fun requestAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, false)
    }

    fun retryAdditional() = viewModelScope.launch {
        githubRepository.requestAdditionalRepos(userName, true)
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followRepos(userName).collect {
            it.doAction(
                onFixed = {
                    it.content.doAction(
                        onExist = { _githubRepos ->
                            githubRepos.value = _githubRepos
                            isMainLoading.value = false
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = null
                        },
                        onNotExist = {
                            githubRepos.value = emptyList()
                            isMainLoading.value = true
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = null
                        }
                    )
                },
                onLoading = {
                    it.content.doAction(
                        onExist = { _githubRepos ->
                            githubRepos.value = _githubRepos
                            isMainLoading.value = false
                            isAdditionalLoading.value = true
                            mainError.value = null
                            additionalError.value = null
                        },
                        onNotExist = {
                            githubRepos.value = emptyList()
                            isMainLoading.value = true
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = null
                        }
                    )
                },
                onError = { exception ->
                    it.content.doAction(
                        onExist = { _githubRepos ->
                            githubRepos.value = _githubRepos
                            isMainLoading.value = false
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = exception
                        },
                        onNotExist = {
                            githubRepos.value = emptyList()
                            isMainLoading.value = false
                            isAdditionalLoading.value = false
                            mainError.value = exception
                            additionalError.value = null
                        }
                    )
                }
            )
        }
    }
}
