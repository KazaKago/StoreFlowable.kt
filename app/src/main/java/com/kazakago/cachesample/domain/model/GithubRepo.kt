package com.kazakago.cachesample.domain.model

import java.io.Serializable
import java.net.URL

data class GithubRepo(
    val id: GithubRepoId,
    val name: String,
    val url: URL
) : Serializable