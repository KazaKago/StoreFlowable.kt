import org.jetbrains.kotlin.config.KotlinCompilerVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("com.novoda.bintray-release")
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
    groupId = "com.kazakago.cacheflowable"
    artifactId = "cacheflowable-core"
    publishVersion = versionName
    desc = "Repository pattern support library for Kotlin with Coroutins & Flow."
    website = "https://github.com/KazaKago/CacheFlowable"
    setLicences("Apache-2.0")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8", KotlinCompilerVersion.VERSION))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.9")
    implementation("com.os.operando.guild.kt:guild_kt:1.0.0")

    testImplementation("junit:junit:4.13")
    testImplementation("org.amshove.kluent:kluent:1.61")
    testImplementation("io.mockk:mockk:1.10.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.3.9")
}
