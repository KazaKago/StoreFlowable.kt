package com.kazakago.cacheflowable.sample.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubRepo(
    @Json(name = "id")
    val id: Long,
    @Json(name = "full_name")
    val fullName: String,
    @Json(name = "html_url")
    val htmlUrl: String
)
