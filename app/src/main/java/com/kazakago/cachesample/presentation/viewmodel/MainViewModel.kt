package com.kazakago.cachesample.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.cachesample.data.repository.GithubRepository
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

    val githubReposState: LiveData<GithubRepoState> get() = _githubReposState
    private val _githubReposState = MutableLiveData<GithubRepoState>(GithubRepoState.Loading)
    val hideSwipeRefresh: UnitLiveEvent get() = _hideSwipeRefresh
    private val _hideSwipeRefresh = MutableUnitLiveEvent()
    val exception: LiveEvent<Exception> get() = _exception
    private val _exception = MutableLiveEvent<Exception>()
    private var shouldNoticeErrorOnNextState: Boolean = false

    init {
        subscribeRepos()
    }

    fun request() = viewModelScope.launch {
        when (githubReposState.value) {
            is GithubRepoState.Loading -> Unit
            is GithubRepoState.LoadingWithValue -> shouldNoticeErrorOnNextState = true
            is GithubRepoState.Completed -> shouldNoticeErrorOnNextState = true
            is GithubRepoState.Error -> Unit
            is GithubRepoState.ErrorWithValue -> shouldNoticeErrorOnNextState = true
        }
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
            _githubReposState.value = when (it) {
                is State.Fixed -> {
                    shouldNoticeErrorOnNextState = false
                    when (it.content) {
                        is StateContent.Exist -> GithubRepoState.Completed(it.content.rawContent)
                        is StateContent.NotExist -> GithubRepoState.Loading
                    }
                }
                is State.Loading -> when (it.content) {
                    is StateContent.Exist -> GithubRepoState.LoadingWithValue(it.content.rawContent)
                    is StateContent.NotExist -> GithubRepoState.Loading
                }
                is State.Error -> {
                    if (shouldNoticeErrorOnNextState) _exception.call(it.exception)
                    shouldNoticeErrorOnNextState = false;
                    when (it.content) {
                        is StateContent.Exist -> GithubRepoState.ErrorWithValue(it.content.rawContent, it.exception)
                        is StateContent.NotExist -> GithubRepoState.Error(it.exception)
                    }
                }
            }
        }
    }

}