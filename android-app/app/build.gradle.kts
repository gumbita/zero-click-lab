plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

val generatedAsanRoot = layout.buildDirectory.dir("generated/asan")

android {
    namespace = "com.echocall.lab"
    compileSdk = 36
    ndkVersion = "27.0.12077973"

    defaultConfig {
        applicationId = "com.echocall.lab"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }

    flavorDimensions += "security"

    productFlavors {
        create("vulnerable") {
            dimension = "security"
            applicationId = "com.echocall.lab.vulnerable"

            externalNativeBuild {
                cmake {
                    arguments +=
                        "-DECHOCALL_PARSER_IMPLEMENTATION=VULNERABLE"
                }
            }
        }

        create("patched") {
            dimension = "security"
            applicationId = "com.echocall.lab.patched"

            externalNativeBuild {
                cmake {
                    arguments +=
                        "-DECHOCALL_PARSER_IMPLEMENTATION=PATCHED"
                }
            }
        }
    }

    buildTypes {
        create("asan") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".asan"
            isDebuggable = true
            matchingFallbacks += listOf("debug")

            ndk {
                abiFilters += listOf("x86_64")
            }

            externalNativeBuild {
                cmake {
                    arguments += "-DENABLE_ANDROID_ASAN=ON"
                }
            }
        }

        release {
            isMinifyEnabled = false
        }
    }

    sourceSets {
        getByName("asan") {
            jniLibs.srcDir(generatedAsanRoot.map { it.dir("jniLibs") })
            resources.srcDir(generatedAsanRoot.map { it.dir("resources") })
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

val ndkDirectory = androidComponents.sdkComponents.ndkDirectory
val asanRuntime = ndkDirectory.map { ndk ->
    val matches = ndk.asFileTree.matching {
        include(
            "toolchains/llvm/prebuilt/*/lib/clang/*/lib/linux/" +
                "libclang_rt.asan-x86_64-android.so",
        )
    }.files

    require(matches.size == 1) {
        "Expected exactly one x86_64 ASan runtime in ${ndk.asFile}, " +
            "found ${matches.size}"
    }
    matches.single()
}
val asanWrapper = ndkDirectory.map { ndk ->
    ndk.file("wrap.sh/asan.sh").asFile
}

val prepareAsanRuntime by tasks.registering(Sync::class) {
    from(asanRuntime) {
        into("jniLibs/x86_64")
    }
    from(asanWrapper) {
        into("resources/lib/x86_64")
        rename("asan.sh", "wrap.sh")
    }
    into(generatedAsanRoot)
}

tasks.configureEach {
    if (
        name == "preVulnerableAsanBuild" ||
        name == "prePatchedAsanBuild"
    ) {
        dependsOn(prepareAsanRuntime)
    }
}

androidComponents {
    beforeVariants(selector().withBuildType("release")) {
        it.enable = false
    }

    onVariants(selector().withBuildType("asan")) { variant ->
        variant.packaging.jniLibs.useLegacyPackaging.set(true)
        variant.packaging.jniLibs.keepDebugSymbols.add(
            "**/libclang_rt.asan-x86_64-android.so",
        )
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.core:core-ktx:1.16.0")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.navigation:navigation-compose:2.9.8")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    debugImplementation("androidx.compose.ui:ui-tooling")
}
