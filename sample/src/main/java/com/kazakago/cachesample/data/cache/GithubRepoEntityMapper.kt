package com.kazakago.cachesample.data.cache

import com.kazakago.cachesample.domain.model.GithubRepo
import com.kazakago.cachesample.domain.model.GithubRepoId
import java.net.URL

class GithubRepoEntityMapper {
    fun map(source: GithubRepoEntity): GithubRepo {
        return GithubRepo(
            id = GithubRepoId(source.id),
            name = source.name,
            url = URL(source.url)
        )
    }

    fun reverse(source: GithubRepo): GithubRepoEntity {
        return GithubRepoEntity(
            id = source.id.value,
            name = source.name,
            url = source.url.toString()
        )
    }
}