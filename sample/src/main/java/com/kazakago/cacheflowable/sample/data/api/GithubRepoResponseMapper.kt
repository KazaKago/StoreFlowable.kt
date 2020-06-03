package com.kazakago.cacheflowable.sample.data.api

import com.kazakago.cacheflowable.sample.data.cache.GithubRepoEntity

class GithubRepoResponseMapper {
    fun map(source: GithubRepoResponse): GithubRepoEntity {
        return GithubRepoEntity(
            id = source.id,
            name = source.fullName,
            url = source.htmlUrl
        )
    }
}