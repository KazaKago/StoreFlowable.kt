package com.kazakago.cachesample.data.api

import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.GithubRepoId
import java.net.URL

class GithubRepoResponseMapper {
    fun map(source: GithubRepoResponse): GithubRepo {
        return GithubRepo(
            id = GithubRepoId(source.id),
            name = source.fullName,
            url = URL(source.htmlUrl)
        )
    }
}