package com.kazakago.cacheflowable.sample.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubOrg(
    @Json(name = "id")
    val id: Long,
    @Json(name = "login")
    val name: String,
)
