package com.kazakago.cachesample.domain.usecase

import com.kazakago.cachesample.data.repository.GithubRepository
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.state.State
import kotlinx.coroutines.flow.Flow

class SubscribeGithubReposUseCase(private val githubRepository: GithubRepository) {

    operator fun invoke(userName: String): Flow<State<List<GithubRepo>>> {
        return githubRepository.subscribeRepos(userName)
    }

}