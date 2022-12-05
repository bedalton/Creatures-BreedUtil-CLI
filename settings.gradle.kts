
rootProject.name = "BreedUtilCLI"


pluginManagement {
    val kotlinVersion: String by settings
    plugins {
        kotlin("multiplatform") version kotlinVersion
    }
}