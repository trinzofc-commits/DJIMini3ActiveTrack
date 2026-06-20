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
        maven { url = uri("https://artifactory.djisdk.com/artifactory/maven-releases") }
        maven { url = uri("https://jitpack.io") }
    }
}

rootProject.name = "DJIMini3ActiveTrack"
include(":app")
