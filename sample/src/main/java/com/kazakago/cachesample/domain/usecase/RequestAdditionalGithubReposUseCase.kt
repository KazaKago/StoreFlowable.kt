package com.kazakago.cachesample.domain.usecase

import com.kazakago.cachesample.data.repository.GithubRepository

class RequestAdditionalGithubReposUseCase(private val githubRepository: GithubRepository) {

    suspend operator fun invoke(userName: String, fetchOnError: Boolean) {
        githubRepository.requestAdditionalRepos(userName, fetchOnError)
    }

}