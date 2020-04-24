package com.kazakago.cachesample.data.api

import com.kazakago.cachesample.data.cache.GithubUserEntity

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