package com.kazakago.cacheflowable.sample.domain.usecase

import com.kazakago.cacheflowable.sample.data.repository.GithubRepository

class RequestGithubUserUseCase(private val githubRepository: GithubRepository) {

    suspend operator fun invoke(userName: String) {
        githubRepository.requestUser(userName)
    }

}