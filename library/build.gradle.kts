import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("java-library")
    id("com.github.panpf.bintray-publish")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.jvmTarget = "1.8"
}

publish {
    val versionName: String by project
    userOrg = "kazakago"
    groupId = "com.kazakago.storeflowable"
    artifactId = "storeflowable"
    publishVersion = versionName
    desc = "Repository pattern support library for Kotlin with Coroutines & Flow."
    website = "https://github.com/KazaKago/StoreFlowable"
    setLicences("Apache-2.0")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    api(project(":library-core"))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.2")

    testImplementation("junit:junit:4.13.1")
    testImplementation("org.amshove.kluent:kluent:1.64")
    testImplementation("io.mockk:mockk:1.10.3-jdk8")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.2")
}
