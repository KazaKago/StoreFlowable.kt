package com.kazakago.storeflowable.sample.viewmodel

import android.app.Application
import androidx.lifecycle.*
import com.kazakago.storeflowable.sample.model.GithubRepo
import com.kazakago.storeflowable.sample.repository.GithubRepository
import com.kazakago.storeflowable.sample.viewmodel.livedata.MutableLiveEvent
import com.kazakago.storeflowable.sample.viewmodel.livedata.MutableUnitLiveEvent
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
    val mainError = MutableLiveData<Exception?>()
    val additionalError = MutableLiveData<Exception?>()
    val strongError = MutableLiveEvent<Exception>()
    val hideSwipeRefresh = MutableUnitLiveEvent()
    private val githubRepository = GithubRepository()
    private var shouldNoticeErrorOnNextState: Boolean = false

    init {
        subscribe()
    }

    fun request() = viewModelScope.launch {
        if (!githubRepos.value.isNullOrEmpty()) shouldNoticeErrorOnNextState = true
        githubRepository.requestRepos(userName)
        hideSwipeRefresh.call()
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
                    shouldNoticeErrorOnNextState = false
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
                    if (shouldNoticeErrorOnNextState) strongError.call(exception)
                    shouldNoticeErrorOnNextState = false
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
