package com.kazakago.storeflowable.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubUser(
    @Json(name = "id")
    val id: Long,
    @Json(name = "name")
    val name: String,
    @Json(name = "html_url")
    val htmlUrl: String,
    @Json(name = "avatar_url")
    val avatarUrl: String
)
