package com.kazakago.cachesample.domain.usecase

import com.kazakago.cachesample.data.repository.GithubRepository
import com.kazakago.cachesample.domain.model.GithubUser
import com.kazakago.cachesample.domain.model.state.State
import kotlinx.coroutines.flow.Flow

class SubscribeGithubUserUseCase(private val githubRepository: GithubRepository) {

    operator fun invoke(userName: String): Flow<State<GithubUser>> {
        return githubRepository.subscribeUser(userName)
    }

}