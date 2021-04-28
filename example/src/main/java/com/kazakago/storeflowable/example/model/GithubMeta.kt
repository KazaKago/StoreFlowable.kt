package com.kazakago.storeflowable.example.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GithubMeta(
    @Json(name = "verifiable_password_authentication")
    val verifiablePasswordAuthentication: Boolean,
    @Json(name = "ssh_key_fingerprints")
    val sshKeyFingerprints: SshKeyFingerprints,
) {
    @JsonClass(generateAdapter = true)
    data class SshKeyFingerprints(
        @Json(name = "SHA256_RSA")
        val sha256Rsa: String,
        @Json(name = "SHA256_DSA")
        val sha256Dsa: String,
    )
}
