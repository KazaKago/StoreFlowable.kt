// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val versionName by extra("3.2.0-SNAPSHOT")
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://plugins.gradle.org/m2/") }
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath(kotlin("gradle-plugin", "1.4.32"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.4.30")
        classpath("org.danilopianini:publish-on-central:0.4.3")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
