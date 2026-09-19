rootProject.name = "NapoliKmmBase"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        mavenLocal()

        maven {
            name = "GitHubPackagesLogger"
            url = uri("https://maven.pkg.github.com/elNapoli/kmm-logger")
            credentials {
                val localProperties = java.util.Properties().apply {
                    val file = File(rootDir, "local.properties")
                    if (file.exists()) load(file.inputStream())
                }
                username = localProperties.getProperty("gpr.user")
                    ?: System.getenv("GITHUB_ACTOR")
                password = localProperties.getProperty("gpr.token")
                    ?: System.getenv("PAT_READ_PACKAGES")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }

        maven {
            name = "GitHubPackagesNavigation"
            url = uri("https://maven.pkg.github.com/elNapoli/kmm-navigation")
            credentials {
                val localProperties = java.util.Properties().apply {
                    val file = File(rootDir, "local.properties")
                    if (file.exists()) load(file.inputStream())
                }
                username = localProperties.getProperty("gpr.user")
                    ?: System.getenv("GITHUB_ACTOR")
                password = localProperties.getProperty("gpr.token")
                    ?: System.getenv("PAT_READ_PACKAGES")
                    ?: System.getenv("GITHUB_TOKEN")
            }
        }
    }
}

include(":base-kmp-domain")
include(":base-kmp-data")
include(":base-kmp-presentation")