package com.kazakago.cachesample.data.api

import com.kazakago.cachesample.data.cache.GithubRepoEntity
import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.GithubRepoId
import java.net.URL

class GithubRepoResponseMapper {
    fun map(source: GithubRepoResponse): GithubRepoEntity {
        return GithubRepoEntity(
            id = source.id,
            name = source.fullName,
            url = source.htmlUrl
        )
    }
}