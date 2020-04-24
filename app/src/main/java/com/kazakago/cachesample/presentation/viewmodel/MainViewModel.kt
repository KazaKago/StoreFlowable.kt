package com.kazakago.cachesample.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.cachesample.data.repository.GithubRepository
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.State
import com.kazakago.cachesample.domain.model.StateContent
import com.kazakago.cachesample.domain.usecase.RequestAdditionalGithubReposUseCase
import com.kazakago.cachesample.domain.usecase.RequestGithubReposUseCase
import com.kazakago.cachesample.domain.usecase.SubscribeGithubReposUseCase
import com.kazakago.cachesample.presentation.viewmodel.livedata.LiveEvent
import com.kazakago.cachesample.presentation.viewmodel.livedata.MutableLiveEvent
import com.kazakago.cachesample.presentation.viewmodel.livedata.MutableUnitLiveEvent
import com.kazakago.cachesample.presentation.viewmodel.livedata.UnitLiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val USER_NAME = "google"
    }

    private val subscribeGithubReposUseCase = SubscribeGithubReposUseCase(GithubRepository())
    private val requestGithubReposUseCase = RequestGithubReposUseCase(GithubRepository())
    private val requestAdditionalGithubReposUseCase = RequestAdditionalGithubReposUseCase(GithubRepository())
    val githubRepos: LiveData<List<GithubRepo>> get() = _githubRepos
    private val _githubRepos = MutableLiveData<List<GithubRepo>>(emptyList())
    val isMainLoading: LiveData<Boolean> get() = _isMainLoading
    private val _isMainLoading = MutableLiveData(false)
    val isAdditionalLoading: LiveData<Boolean> get() = _isAdditionalLoading
    private val _isAdditionalLoading = MutableLiveData(false)
    val mainError: LiveData<Exception?> get() = _mainError
    private val _mainError = MutableLiveData<Exception?>()
    val additionalError: LiveData<Exception?> get() = _additionalError
    private val _additionalError = MutableLiveData<Exception?>()
    val strongError: LiveEvent<Exception> get() = _strongError
    private val _strongError = MutableLiveEvent<Exception>()
    val hideSwipeRefresh: UnitLiveEvent get() = _hideSwipeRefresh
    private val _hideSwipeRefresh = MutableUnitLiveEvent()
    private var shouldNoticeErrorOnNextState: Boolean = false

    init {
        subscribeRepos()
    }

    fun request() = viewModelScope.launch {
        if (!githubRepos.value.isNullOrEmpty()) shouldNoticeErrorOnNextState = true
        requestGithubReposUseCase(USER_NAME)
        _hideSwipeRefresh.call()
    }

    fun requestAdditional(fetchOnError: Boolean = false) = viewModelScope.launch {
        requestAdditionalGithubReposUseCase(USER_NAME, fetchOnError)
    }

    fun retryAdditional() {
        requestAdditional(true)
    }

    private fun subscribeRepos() = viewModelScope.launch {
        subscribeGithubReposUseCase(USER_NAME).collect {
            when (it) {
                is State.Fixed -> {
                    shouldNoticeErrorOnNextState = false
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubRepos.value = it.content.rawContent
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = null
                        }
                        is StateContent.NotExist -> {
                            _githubRepos.value = emptyList()
                            _isMainLoading.value = true
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = null
                        }
                    }
                }
                is State.Loading -> {
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubRepos.value = it.content.rawContent
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = true
                            _mainError.value = null
                            _additionalError.value = null
                        }
                        is StateContent.NotExist -> {
                            _githubRepos.value = emptyList()
                            _isMainLoading.value = true
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = null
                        }
                    }
                }
                is State.Error -> {
                    if (shouldNoticeErrorOnNextState) _strongError.call(it.exception)
                    shouldNoticeErrorOnNextState = false;
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubRepos.value = it.content.rawContent
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = null
                            _additionalError.value = it.exception
                        }
                        is StateContent.NotExist -> {
                            _githubRepos.value = emptyList()
                            _isMainLoading.value = false
                            _isAdditionalLoading.value = false
                            _mainError.value = it.exception
                            _additionalError.value = null
                        }
                    }
                }
            }
        }
    }

}