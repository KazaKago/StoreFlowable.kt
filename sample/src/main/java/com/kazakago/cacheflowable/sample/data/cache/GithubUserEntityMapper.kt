package com.kazakago.cacheflowable.sample.data.cache

import com.kazakago.cacheflowable.sample.domain.model.GithubUser
import com.kazakago.cacheflowable.sample.domain.model.GithubUserId
import java.net.URL

class GithubUserEntityMapper {
    fun map(source: GithubUserEntity): GithubUser {
        return GithubUser(
            id = GithubUserId(source.id),
            name = source.name,
            url = URL(source.url),
            avatarUrl = URL(source.avatarUrl)
        )
    }

    fun reverse(source: GithubUser): GithubUserEntity {
        return GithubUserEntity(
            id = source.id.value,
            name = source.name,
            url = source.url.toString(),
            avatarUrl = source.avatarUrl.toString()
        )
    }
}