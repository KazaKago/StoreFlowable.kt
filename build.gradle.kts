// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    val versionName by extra("3.2.0-SNAPSHOT")
    repositories {
        google()
        jcenter()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.1.3")
        classpath(kotlin("gradle-plugin", "1.4.32"))
        classpath("com.github.panpf.bintray-publish:bintray-publish:1.0.0")

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
