import org.gradle.api.Project
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.signing.SigningExtension

private fun Project.publishing(configure: PublishingExtension.() -> Unit) = configure(configure)

private fun Project.signing(configure: SigningExtension.() -> Unit) = configure(configure)

fun Project.setupPublishing(
    version: String = PublishingInfo.versionName,
    groupId: String = PublishingInfo.groupId,
    projectName: String = PublishingInfo.projectName,
    projectDescription: String = PublishingInfo.projectDescription,
    licenseName: String = PublishingInfo.licenseName,
    licenseUrl: String = PublishingInfo.licenseUrl,
    projectUrl: String = PublishingInfo.projectUrl,
    scmConnection: String = PublishingInfo.scmConnection,
    developerName: String = PublishingInfo.developerName,
    developerEmail: String = PublishingInfo.developerEmail,
    developerUrl: String = PublishingInfo.developerUrl,
) {
    this.version = version
    this.group = groupId
    val javadocJar by tasks.registering(Jar::class) {
        group = "publishing"
        archiveClassifier.set("javadoc")
        from(tasks["dokkaHtml"])
    }
    publishing {
        publications.withType<MavenPublication>().all {
            artifact(javadocJar.get())
            pom {
                name.set(projectName)
                description.set(projectDescription)
                url.set(projectUrl)
                licenses {
                    license {
                        name.set(licenseName)
                        url.set(licenseUrl)
                    }
                }
                scm {
                    connection.set(scmConnection)
                    developerConnection.set(scmConnection)
                    url.set(projectUrl)
                }
                developers {
                    developer {
                        name.set(developerName)
                        email.set(developerEmail)
                        url.set(developerUrl)
                    }
                }
            }
            signing {
                val keyId = System.getenv("SIGNING_KEY_ID") ?: properties["signing.keyId"]?.toString()
                val secretKey = System.getenv("SIGNING_SECRET_KEY") ?: properties["signing.secretKey"]?.toString()
                val password = System.getenv("SIGNING_PASSWORD") ?: properties["signing.password"]?.toString()
                useInMemoryPgpKeys(keyId, secretKey, password)
                sign(this@all)
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                url = if (version.endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = System.getenv("SONATYPE_USERNAME") ?: properties["sonatype.username"]?.toString()
                    password = System.getenv("SONATYPE_PASSWORD") ?: properties["sonatype.password"]?.toString()
                }
            }
        }
    }
}
