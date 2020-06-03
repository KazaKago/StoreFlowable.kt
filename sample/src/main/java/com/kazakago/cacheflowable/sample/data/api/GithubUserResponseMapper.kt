package com.kazakago.cacheflowable.sample.data.api

import com.kazakago.cacheflowable.sample.data.cache.GithubUserEntity

class GithubUserResponseMapper {
    fun map(source: GithubUserResponse): GithubUserEntity {
        return GithubUserEntity(
            id = source.id,
            name = source.name,
            url = source.htmlUrl,
            avatarUrl = source.avatarUrl
        )
    }
}