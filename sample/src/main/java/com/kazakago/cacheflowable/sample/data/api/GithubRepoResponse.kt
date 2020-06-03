package com.kazakago.cacheflowable.sample.data.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubRepoResponse(
    @Json(name = "id")
    val id: Long,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "html_url")
    val htmlUrl: String
)