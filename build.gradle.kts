@file:Suppress("UNUSED_VARIABLE")

plugins {
    id("com.bedalton.multiplatform") version "1.0.0"
    kotlin("multiplatform")
    application
}

val projectGroup: String by project
val projectVersion: String by project
val projectVersionForNPM: String by project
val projectGithubUrl: String by project

group = projectGroup
version = projectVersion

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
    @Suppress("DEPRECATION")
    jcenter()
}

// Kotlin / KotlinX
val kotlinVersion: String by project
val kotlinxCoroutinesVersion: String by project
val kotlinxCliVersion: String by project

// Creatures
val creaturesCommonCLIVersion: String by project
val creaturesCommonCoreVersion: String by project
val creaturesGenomeUtilVersion: String by project
val creaturesBreedUtilVersion: String by project
val creaturesSpriteUtilVersion: String by project

// Bedalton
val bedaltonLocalFilesVersion: String by project
val bedaltonAppSupportVersion: String by project
val bedaltonByteUtilVersion: String by project
val bedaltonCommonLogVersion: String by project
val bedaltonCommonCoreVersion: String by project
val bedaltonCommonCoroutinesVersion: String by project

bedaltonConfig {
    additionalLibraries += listOf("breed-util")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }
    js(IR) {
        useCommonJs()
        binaries.executable()
        moduleName = "breed-util-cli"
        nodejs {
            useCommonJs()
            binaries.library()

            compilations["main"].apply {
                packageJson {
                    name = "breed-util-cli"// + (if (isDevelopment) "-dev" else "")
                    description = "Command line utility for converting breeds between Creatures games"
//                    main = "breed-util-node.js"
                    version = projectVersionForNPM
                    customField("license", "MIT")
                    customField(
                        "repository", mapOf(
                            "type" to "git",
                            "url" to "git+$projectGithubUrl.git"
                        )
                    )
                    customField("bin", mapOf("breed-util" to "./breed-util-main.js"))
                    customField("author", "bedalton")
                    customField("bugs", mapOf("url" to "$projectGithubUrl/issues"))
                    customField("homepage", "$projectGithubUrl#readme")
                    customField(
                        "scripts", mapOf(
                            "package" to "npm install && pkg package.json"
                        )
                    )
                    customField(
                        "pkg", mapOf(
                            "assets" to listOf(
                                "genomes/*"
                            ),
                            "scripts" to listOf(
                                "./breed-util-main.js"
                            ),
                            "output" to "breed-util.exe",
                            "targets" to listOf("node12-win-x64")
                        )
                    )
                    customField(
                        "keywords", listOf(
                            "Creatures",
                            "C16",
                            "S16",
                            "SPR",
                            "BLK",
                            "GEN",
                            "GNO",
                            "Creatures1",
                            "Creatures2",
                            "Creatures3",
                            "Docking Station",
                            "DockingStation",
                            "Sprite",
                            "ATT",
                            "Breed Converter",
                            "Breed Conversion"
                        )
                    )
                }
            }
        }
    }
    macosX64() {
        binaries.executable {
            this.entryPoint = "bedalton.creatures.breed.converter.cli.main"
        }
    }
    linuxX64() {
        binaries.executable {
            this.entryPoint = "bedalton.creatures.breed.converter.cli.main"
        }
    }
    mingwX64() {

        binaries.executable {
            this.entryPoint = "bedalton.creatures.breed.converter.cli.main"
        }
    }
    sourceSets {

        val commonMain by getting {
            dependencies {

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinxCoroutinesVersion")
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion") {
                    exclude("com.bedalton", "kotlinx-nodejs")
                }
                implementation("bedalton.creatures:creatures-common-cli:$creaturesCommonCLIVersion") {
                    exclude("com.bedalton", "kotlinx-nodejs")
                }
                implementation("bedalton.creatures:creatures-common:$creaturesCommonCoreVersion")
                implementation("bedalton.creatures:breed-util:$creaturesBreedUtilVersion")
                implementation("bedalton.creatures:common-genome:$creaturesGenomeUtilVersion")
                implementation("bedalton.creatures:common-sprite:$creaturesSpriteUtilVersion")
                implementation("com.bedalton:local-files:$bedaltonLocalFilesVersion")
                implementation("com.bedalton:app-support:$bedaltonAppSupportVersion")
                implementation("com.bedalton:common-log:$bedaltonCommonLogVersion")
                implementation("com.bedalton:common-core:$bedaltonCommonCoreVersion")
                implementation("com.bedalton:common-byte:$bedaltonByteUtilVersion")
                implementation("com.bedalton:common-coroutines:$bedaltonCommonCoroutinesVersion")
            }
        }

        val commonTest by getting{
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinxCoroutinesVersion")
            }
        }
        val jvmMain by getting
        val jvmTest by getting
        val jsMain by getting {
            dependencies {
                implementation(npm("glob", "7.2.0"))
            }
        }
        val jsTest by getting
        val macosX64Main by getting
        val macosX64Test by getting
        val linuxX64Main by getting
        val linuxX64Test by getting
        val mingwX64Main by getting
        val mingwX64Test by getting

        all {
            languageSettings.optIn("kotlin.RequiresOptIn")
            languageSettings.optIn("kotlin.ExperimentalMultiplatform")
            languageSettings.optIn("kotlin.ExperimentalUnsignedTypes")
            languageSettings.optIn("kotlin.contracts.ExperimentalContracts")
            languageSettings.optIn("kotlin.js.ExperimentalJsExport")
            languageSettings.optIn("kotlinx.cli.ExperimentalCli")
            languageSettings.optIn("kotlinx.coroutines.DelicateCoroutinesApi")
            languageSettings.optIn("kotlinx.coroutines.ExperimentalCoroutinesApi")
            configurations.all {
                resolutionStrategy {

                    eachDependency {
                        if (requested.group == "org.jetbrains.kotlin" && requested.name.startsWith("kotlin-"))
                            useVersion(kotlinVersion)
                    }
                }
            }
        }
    }
}

application {
    mainClass.set("bedalton.creatures.breed.convert.cli.MainKt")
}