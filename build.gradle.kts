@file:Suppress("UNUSED_VARIABLE")

plugins {
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
        moduleName = "breed-util"
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
                implementation("org.jetbrains.kotlinx:kotlinx-cli:$kotlinxCliVersion")
                implementation("bedalton.creatures:CommonCLI:$creaturesCommonCLIVersion")
                implementation("bedalton.creatures:CommonCore:$creaturesCommonCoreVersion")
                implementation("bedalton.creatures:BreedUtil:$creaturesBreedUtilVersion")
                implementation("bedalton.creatures:GenomeUtil:$creaturesGenomeUtilVersion")
                implementation("bedalton.creatures:SpriteUtil:$creaturesSpriteUtilVersion")
                implementation("com.bedalton:LocalFiles:$bedaltonLocalFilesVersion")
                implementation("com.bedalton:AppSupport:$bedaltonAppSupportVersion")
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

configurations.matching { it.name != "kotlinCompilerPluginClasspath" }.all {
    val suffix = when {
        this.name.startsWith("js") -> "node"
        else -> null
    }

    if (suffix != null) {
        resolutionStrategy.eachDependency {
            if (!requested.group.contains("bedalton")) {
                return@eachDependency
            }
            if (
                requested.name.startsWith("CommonCLI") ||
                requested.name.startsWith("LocalFiles") ||
                requested.name.startsWith("BreedUtil") ||
                requested.name.startsWith("AppSupport")
            ) {
                if (!requested.name.endsWith("-$suffix")) {
                    println("Adding suffix to: ${requested.name}")
                    useTarget("${requested.group}:${requested.name}-$suffix:${requested.version}")
                }
            }
        }
    }
}





val copyResourcesTask: (target: String, kind: String) -> Copy = { target: String, kind: String ->
    val create = tasks.create<Copy>("copy${target.capitalize()}${kind.capitalize()}Resources") {
        from("src/${target}${kind.capitalize()}/resources", "src/common${kind.capitalize()}/resources")
        into("build/processedResources/${target}/main")
    }
    create
}

// JS Resources
val copyResourceJS = copyResourcesTask("js", "main")
val copyTestResourcesJS = copyResourcesTask("js", "test")
tasks.getByName("compileKotlinJs")
    .dependsOn(copyResourceJS)
tasks.getByName("jsTest")
    .dependsOn(copyTestResourcesJS)

// MacOS Resources
val copyResourcesMacOs = copyResourcesTask("macosX64", "main")
val copyTestResourcesMacosX64 = copyResourcesTask("macosX64", "test")
tasks.findByName("compileKotlinMacosX64")!!
    .dependsOn(copyResourcesMacOs)
tasks.findByName("macosX64Test")!!
    .dependsOn(copyTestResourcesMacosX64)

// Mingw Resources
val copyResourcesMingw = copyResourcesTask("mingwX64", "main")
val copyTestResourcesMingW = copyResourcesTask("mingwX64", "test")
tasks.findByName("compileKotlinMingwX64")!!
    .dependsOn(copyResourcesMingw)
tasks.findByName("mingwX64Test")!!
    .dependsOn(copyTestResourcesMingW)
