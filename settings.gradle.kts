
rootProject.name = "breed-util-cli"


pluginManagement {
    val kotlinVersion: String by settings
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}