package com.kazakago.cacheflowable.sample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.cacheflowable.sample.model.GithubMeta
import com.kazakago.cacheflowable.sample.repository.GithubRepository
import com.kazakago.cacheflowable.sample.viewmodel.livedata.MutableLiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubMetaViewModel(application: Application) : AndroidViewModel(application) {

    val githubMeta = MutableLiveData<GithubMeta?>()
    val isLoading = MutableLiveData(false)
    val error = MutableLiveData<Exception?>()
    val strongError = MutableLiveEvent<Exception>()
    private val githubRepository = GithubRepository()
    private var shouldNoticeErrorOnNextState: Boolean = false

    init {
        subscribe()
    }

    fun request() = viewModelScope.launch {
        if (githubMeta.value != null) shouldNoticeErrorOnNextState = true
        githubRepository.requestMeta()
    }

    private fun subscribe() = viewModelScope.launch {
        githubRepository.followMeta().collect {
            it.doAction(
                onFixed = {
                    shouldNoticeErrorOnNextState = false
                    it.content.doAction(
                        onExist = { _githubMeta ->
                            githubMeta.value = _githubMeta
                            isLoading.value = false
                            error.value = null
                        },
                        onNotExist = {
                            githubMeta.value = null
                            isLoading.value = false
                            error.value = null
                        }
                    )
                },
                onLoading = {
                    it.content.doAction(
                        onExist = { _githubMeta ->
                            githubMeta.value = _githubMeta
                            isLoading.value = true
                            error.value = null
                        },
                        onNotExist = {
                            githubMeta.value = null
                            isLoading.value = true
                            error.value = null
                        }
                    )
                },
                onError = { exception ->
                    if (shouldNoticeErrorOnNextState) strongError.call(exception)
                    shouldNoticeErrorOnNextState = false
                    it.content.doAction(
                        onExist = { _githubMeta ->
                            githubMeta.value = _githubMeta
                            isLoading.value = false
                            error.value = null
                        },
                        onNotExist = {
                            githubMeta.value = null
                            isLoading.value = false
                            error.value = exception
                        }
                    )
                }
            )
        }
    }

}