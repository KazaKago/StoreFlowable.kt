import org.danilopianini.gradle.mavencentral.mavenCentral
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    id("org.jetbrains.dokka")
    id("org.danilopianini.publish-on-central")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType(KotlinCompile::class).all {
    kotlinOptions.jvmTarget = "1.8"
}

publishOnCentral {
    val versionName: String by project
    projectLongName = PublishingInfo.projectName
    projectDescription = PublishingInfo.projectDescription
    licenseName = PublishingInfo.licenseName
    licenseUrl = PublishingInfo.licenseUrl
    projectUrl = PublishingInfo.projectUrl
    scmConnection = PublishingInfo.scmConnection
    publishing {
        publications {
            withType<MavenPublication> {
                configurePomForMavenCentral()
                groupId = PublishingInfo.groupId
                artifactId = "storeflowable-core"
                version = versionName
                pom {
                    developers {
                        developer {
                            name.set(PublishingInfo.developerName)
                            email.set(PublishingInfo.developerEmail)
                            url.set(PublishingInfo.developerUrl)
                        }
                    }
                }
            }
        }
    }
    val releasesRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2"
    val snapshotsRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots"
    val repositoryUrl = if (versionName.endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
    repository(repositoryUrl) {
        user = mavenCentral().user()
        password = mavenCentral().password()
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.3")
    implementation("com.os.operando.guild.kt:guild_kt:1.0.0")

    testImplementation("junit:junit:4.13.2")
    testImplementation("org.amshove.kluent:kluent:1.65")
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.4.3")
}
