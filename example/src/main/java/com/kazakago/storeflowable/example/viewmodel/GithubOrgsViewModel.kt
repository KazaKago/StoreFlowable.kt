package com.kazakago.storeflowable.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.repository.GithubRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubOrgsViewModel(application: Application) : AndroidViewModel(application) {

    val githubOrgs = MutableLiveData<List<GithubOrg>>(emptyList())
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
        githubRepository.refreshOrgs()
        isRefreshing.value = false
    }

    fun retry() = viewModelScope.launch {
        githubRepository.refreshOrgs()
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
