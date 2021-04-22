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

tasks.create("javadocJar", Jar::class) {
    group = "publishing"
    dependsOn("dokkaJavadoc")
    archiveClassifier.set("javadoc")
    buildDir.resolve("dokka/javadoc")
}

tasks.create("sourcesJar", Jar::class) {
    group = "publishing"
    dependsOn("classes")
    archiveClassifier.set("sources")
    from(sourceSets["main"].allSource)
}

publishing {
    val versionName: String by project
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])
            groupId = PublishingInfo.groupId
            artifactId = "storeflowable-core"
            version = versionName
            pom {
                name.set(PublishingInfo.projectName)
                description.set(PublishingInfo.projectDescription)
                url.set(PublishingInfo.projectUrl)
                licenses {
                    license {
                        name.set(PublishingInfo.licenseName)
                        url.set(PublishingInfo.licenseUrl)
                    }
                }
                scm {
                    connection.set(PublishingInfo.scmConnection)
                    developerConnection.set(PublishingInfo.scmConnection)
                    url.set(PublishingInfo.projectUrl)
                }
                developers {
                    developer {
                        name.set(PublishingInfo.developerName)
                        email.set(PublishingInfo.developerEmail)
                        url.set(PublishingInfo.developerUrl)
                    }
                }
            }
            signing {
                sign(this@create)
            }
        }
    }
    repositories {
        maven {
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
            url = if (versionName.endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
            credentials {
                username = System.getenv("SONATYPE_USERNAME") ?: project.properties["sonatypeUsername"].toString()
                password = System.getenv("SONATYPE_PASSWORD") ?: project.properties["sonatypePassword"].toString()
            }
        }
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
