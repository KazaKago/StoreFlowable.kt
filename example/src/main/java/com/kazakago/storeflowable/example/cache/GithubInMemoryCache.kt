package com.kazakago.storeflowable.example.cache

import com.kazakago.storeflowable.example.model.GithubMeta
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.example.model.GithubRepo
import com.kazakago.storeflowable.example.model.GithubUser
import java.time.LocalDateTime

object GithubInMemoryCache {
    var metaCache: GithubMeta? = null
    var metaCacheCreatedAt: LocalDateTime? = null

    var orgsCache: List<GithubOrg>? = null
    var orgsCacheCreatedAt: LocalDateTime? = null

    val userCache: MutableMap<String, GithubUser?> = mutableMapOf()
    val userCacheCreateAt: MutableMap<String, LocalDateTime> = mutableMapOf()

    val reposCache: MutableMap<String, List<GithubRepo>?> = mutableMapOf()
    val reposCacheCreatedAt: MutableMap<String, LocalDateTime> = mutableMapOf()
}
