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
    }
}

rootProject.name = "ApiWebView"
include(":app")
include(":core:pokeapi")
include(":core:data")
include(":core:database")
include(":core:model")
include(":core:domain")
