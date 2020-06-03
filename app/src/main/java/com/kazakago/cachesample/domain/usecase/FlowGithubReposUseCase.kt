package com.kazakago.cachesample.domain.usecase

import com.kazakago.cacheflowable.core.State
import com.kazakago.cachesample.data.repository.GithubRepository
import com.kazakago.cachesample.domain.model.GithubRepo
import kotlinx.coroutines.flow.Flow

class FlowGithubReposUseCase(private val githubRepository: GithubRepository) {

    operator fun invoke(userName: String): Flow<State<List<GithubRepo>>> {
        return githubRepository.flowRepos(userName)
    }

}