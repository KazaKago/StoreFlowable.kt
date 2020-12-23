package com.kazakago.storeflowable.sample.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.sample.model.GithubOrg
import com.kazakago.storeflowable.sample.repository.GithubRepository
import com.kazakago.storeflowable.sample.viewmodel.livedata.MutableLiveEvent
import com.kazakago.storeflowable.sample.viewmodel.livedata.MutableUnitLiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubOrgsViewModel(application: Application) : AndroidViewModel(application) {

    val githubOrgs = MutableLiveData<List<GithubOrg>>(emptyList())
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
        if (!githubOrgs.value.isNullOrEmpty()) shouldNoticeErrorOnNextState = true
        githubRepository.requestOrgs()
        hideSwipeRefresh.call()
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
                onFixed = {
                    shouldNoticeErrorOnNextState = false
                    it.content.doAction(
                        onExist = { _githubOrgs ->
                            githubOrgs.value = _githubOrgs
                            isMainLoading.value = false
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = null
                        },
                        onNotExist = {
                            githubOrgs.value = emptyList()
                            isMainLoading.value = true
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = null
                        }
                    )
                },
                onLoading = {
                    it.content.doAction(
                        onExist = { _githubOrgs ->
                            githubOrgs.value = _githubOrgs
                            isMainLoading.value = false
                            isAdditionalLoading.value = true
                            mainError.value = null
                            additionalError.value = null
                        },
                        onNotExist = {
                            githubOrgs.value = emptyList()
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
                        onExist = { _githubOrgs ->
                            githubOrgs.value = _githubOrgs
                            isMainLoading.value = false
                            isAdditionalLoading.value = false
                            mainError.value = null
                            additionalError.value = exception
                        },
                        onNotExist = {
                            githubOrgs.value = emptyList()
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
