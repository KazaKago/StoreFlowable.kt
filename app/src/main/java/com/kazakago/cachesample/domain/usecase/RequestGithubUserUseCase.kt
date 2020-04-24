package com.kazakago.cachesample.domain.usecase

import com.kazakago.cachesample.data.repository.GithubRepository

class RequestGithubUserUseCase(private val githubRepository: GithubRepository) {

    suspend operator fun invoke(userName: String) {
        githubRepository.requestUser(userName)
    }

}