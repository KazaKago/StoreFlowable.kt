package com.kazakago.cacheflowable.sample.domain.usecase

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.sample.data.repository.GithubRepository
import com.kazakago.cacheflowable.sample.domain.model.GithubUser
import kotlinx.coroutines.flow.Flow

class FlowGithubUserUseCase(private val githubRepository: GithubRepository) {

    operator fun invoke(userName: String): Flow<State<GithubUser>> {
        return githubRepository.flowUser(userName)
    }

}