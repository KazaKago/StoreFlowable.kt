package com.kazakago.cacheflowable.sample.domain.usecase

import com.kazakago.cacheflowable.core.State
import com.kazakago.cacheflowable.sample.data.repository.GithubRepository
import com.kazakago.cacheflowable.sample.domain.model.GithubRepo
import kotlinx.coroutines.flow.Flow

class FlowGithubReposUseCase(private val githubRepository: GithubRepository) {

    operator fun invoke(userName: String): Flow<State<List<GithubRepo>>> {
        return githubRepository.flowRepos(userName)
    }

}