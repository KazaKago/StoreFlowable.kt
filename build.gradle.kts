// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val versionName by extra("1.0.0")
    repositories {
        google()
        jcenter()
        maven("https://dl.bintray.com/maranda/maven/")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.0.1")
        classpath(kotlin("gradle-plugin", "1.4.0"))
        classpath("com.novoda:bintray-release:1.0.3")

        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
    }
}

allprojects {
    repositories {
        google()
        jcenter()
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}