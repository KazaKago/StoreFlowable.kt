package com.kazakago.cachesample.presentation.viewmodel

import com.kazakago.cachesample.domain.model.GithubRepo

sealed class GithubRepoState {
    object Loading : GithubRepoState()
    class LoadingWithValue(val githubRepos: List<GithubRepo>) : GithubRepoState()
    class Completed(val githubRepos: List<GithubRepo>) : GithubRepoState()
    class Error(val exception: Exception) : GithubRepoState()
    class ErrorWithValue(val githubRepos: List<GithubRepo>, val exception: Exception) : GithubRepoState()
}
