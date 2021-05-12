import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.get
import org.gradle.plugins.signing.SigningExtension

private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
    (this as ExtensionAware).extensions.configure("publishing", configure)

private fun Project.signing(configure: Action<SigningExtension>): Unit =
    (this as ExtensionAware).extensions.configure("signing", configure)

private val Project.sourceSets: SourceSetContainer
    get() = (this as ExtensionAware).extensions.getByName("sourceSets") as SourceSetContainer

fun Project.setupPublishing(
    artifactId: String,
    version: String,
    groupId: String = PublishingInfo.groupId,
    projectName: String = PublishingInfo.projectName,
    projectDescription: String = PublishingInfo.projectDescription,
    licenseName: String = PublishingInfo.licenseName,
    licenseUrl: String = PublishingInfo.licenseUrl,
    projectUrl: String = PublishingInfo.projectUrl,
    scmConnection: String = PublishingInfo.scmConnection,
    developerName: String = PublishingInfo.developerName,
    developerEmail: String = PublishingInfo.developerEmail,
    developerUrl: String = PublishingInfo.developerUrl
) {
    tasks.create("javadocJar", Jar::class) {
        group = "publishing"
        dependsOn("dokkaJavadoc")
        archiveClassifier.set("javadoc")
        from(buildDir.resolve("dokka/javadoc"))
    }
    tasks.create("sourcesJar", Jar::class) {
        group = "publishing"
        dependsOn("classes")
        archiveClassifier.set("sources")
        from(sourceSets["main"].allSource)
    }
    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])
                artifact(tasks["sourcesJar"])
                artifact(tasks["javadocJar"])
                this.groupId = groupId
                this.artifactId = artifactId
                this.version = version
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
                    val keyId = System.getenv("SIGNING_KEY_ID") ?: properties["signing.keyId"].toString()
                    val secretKey = System.getenv("SIGNING_SECRET_KEY") ?: properties["signing.secretKey"].toString()
                    val password = System.getenv("SIGNING_PASSWORD") ?: properties["signing.password"].toString()
                    useInMemoryPgpKeys(keyId, secretKey, password)
                    sign(this@create)
                }
            }
        }
        repositories {
            maven {
                val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2")
                val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots")
                url = if (version.endsWith("-SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                credentials {
                    username = System.getenv("SONATYPE_USERNAME") ?: properties["sonatype.username"].toString()
                    password = System.getenv("SONATYPE_PASSWORD") ?: properties["sonatype.password"].toString()
                }
            }
        }
    }
}
