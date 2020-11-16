package com.kazakago.cacheflowable.sample.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubMeta(
    @Json(name = "verifiable_password_authentication")
    val id: Boolean,
    @Json(name = "ssh_key_fingerprints")
    val sshKeyFingerprints: SshKeyFingerprints,
) {
    @JsonClass(generateAdapter = true)
    data class SshKeyFingerprints(
        @Json(name = "MD5_RSA")
        val md5Rsa: String,
        @Json(name = "MD5_DSA")
        val md5Dsa: String,
        @Json(name = "SHA256_RSA")
        val sha256Rsa: String,
        @Json(name = "SHA256_DSA")
        val sha256Dsa: String,
    )
}
