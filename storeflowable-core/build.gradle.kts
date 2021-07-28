import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.jvmTarget = "11"
}

val versionName: String by project
setupPublishing(version = versionName, artifactId = "storeflowable-core")

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.1")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.amshove.kluent:kluent:1.68")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.1")
}
