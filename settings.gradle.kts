pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://arsenal.dji.com/artifactory/maven-releases") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DJIMini3ActiveTrack"
include(":app")
