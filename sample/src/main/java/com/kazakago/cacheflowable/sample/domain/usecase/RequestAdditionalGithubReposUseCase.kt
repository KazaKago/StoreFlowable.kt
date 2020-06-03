package com.kazakago.cacheflowable.sample.domain.usecase

import com.kazakago.cacheflowable.sample.data.repository.GithubRepository

class RequestAdditionalGithubReposUseCase(private val githubRepository: GithubRepository) {

    suspend operator fun invoke(userName: String, fetchOnError: Boolean) {
        githubRepository.requestAdditionalRepos(userName, fetchOnError)
    }

}