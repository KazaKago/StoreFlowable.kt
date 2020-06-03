package com.kazakago.cacheflowable.sample.domain.model

import java.io.Serializable
import java.net.URL

data class GithubUser(
    val id: GithubUserId,
    val name: String,
    val url: URL,
    val avatarUrl: URL
) : Serializable