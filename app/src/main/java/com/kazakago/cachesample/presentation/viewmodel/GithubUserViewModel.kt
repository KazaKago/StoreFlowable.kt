package com.kazakago.cachesample.presentation.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.kazakago.cachesample.data.repository.GithubRepository
import com.kazakago.cachesample.domain.model.GithubUser
import com.kazakago.cachesample.domain.model.state.State
import com.kazakago.cachesample.domain.model.state.StateContent
import com.kazakago.cachesample.domain.usecase.RequestGithubUserUseCase
import com.kazakago.cachesample.domain.usecase.SubscribeGithubUserUseCase
import com.kazakago.cachesample.presentation.viewmodel.livedata.LiveEvent
import com.kazakago.cachesample.presentation.viewmodel.livedata.MutableLiveEvent
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class GithubUserViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val USER_NAME = "google"
    }

    private val subscribeGithubUserUseCase = SubscribeGithubUserUseCase(GithubRepository())
    private val requestGithubUserUseCase = RequestGithubUserUseCase(GithubRepository())
    val githubUser: LiveData<GithubUser?> get() = _githubUser
    private val _githubUser = MutableLiveData<GithubUser?>()
    val isLoading: LiveData<Boolean> get() = _isLoading
    private val _isLoading = MutableLiveData(false)
    val error: LiveData<Exception?> get() = _error
    private val _error = MutableLiveData<Exception?>()
    val strongError: LiveEvent<Exception> get() = _strongError
    private val _strongError = MutableLiveEvent<Exception>()
    private var shouldNoticeErrorOnNextState: Boolean = false

    init {
        subscribeRepos()
    }

    fun request() = viewModelScope.launch {
        if (githubUser.value != null) shouldNoticeErrorOnNextState = true
        requestGithubUserUseCase(USER_NAME)
    }

    private fun subscribeRepos() = viewModelScope.launch {
        subscribeGithubUserUseCase(USER_NAME).collect {
            when (it) {
                is State.Fixed -> {
                    shouldNoticeErrorOnNextState = false
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubUser.value = it.content.rawContent
                            _isLoading.value = false
                            _error.value = null
                        }
                        is StateContent.NotExist -> {
                            _githubUser.value = null
                            _isLoading.value = false
                            _error.value = null
                        }
                    }
                }
                is State.Loading -> {
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubUser.value = it.content.rawContent
                            _isLoading.value = true
                            _error.value = null
                        }
                        is StateContent.NotExist -> {
                            _githubUser.value = null
                            _isLoading.value = true
                            _error.value = null
                        }
                    }
                }
                is State.Error -> {
                    if (shouldNoticeErrorOnNextState) _strongError.call(it.exception)
                    shouldNoticeErrorOnNextState = false
                    when (it.content) {
                        is StateContent.Exist -> {
                            _githubUser.value = it.content.rawContent
                            _isLoading.value = false
                            _error.value = null
                        }
                        is StateContent.NotExist -> {
                            _githubUser.value = null
                            _isLoading.value = false
                            _error.value = it.exception
                        }
                    }
                }
            }
        }
    }

}