// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val versionName by extra("4.1.0-SNAPSHOT")
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:7.0.0")
        classpath(kotlin("gradle-plugin", "1.5.21"))
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.5.0")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven { setUrl("https://jitpack.io") }
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
