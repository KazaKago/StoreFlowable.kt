import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    `maven-publish`
    signing
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.jvmTarget = "1.8"
}

val versionName: String by project
setupPublishing(version = versionName, artifactId = "storeflowable")

dependencies {
    api(project(":library-core"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.amshove.kluent:kluent:1.67")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.0")
}
