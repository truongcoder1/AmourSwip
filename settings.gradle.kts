pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
// settings.gradle.kts
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS) // Yêu cầu ưu tiên repository từ settings
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io") }

    }
}

rootProject.name = "AmourSwip"
include(":app")
 