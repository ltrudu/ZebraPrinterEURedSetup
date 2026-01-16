plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.zebra.zebraprintereuredsetup"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.zebra.zebraprintereuredsetup.dev"
        minSdk = 29
        targetSdk = 36
        versionCode = 14
        versionName = "2.1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    packaging {
        resources {
            excludes += setOf(
                "META-INF/LICENSE.txt",
                "META-INF/NOTICE.txt",
                "META-INF/NOTICE",
                "META-INF/LICENSE",
                "META-INF/DEPENDENCIES"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    applicationVariants.all {
        val variant = this
        variant.outputs.all {
            val output = this as com.android.build.gradle.internal.api.BaseVariantOutputImpl
            val versionName = variant.versionName
            val buildType = variant.buildType.name
            output.outputFileName = "ZEURedBTFree-${versionName}-${buildType}.apk"
        }
    }
}

tasks.whenTaskAdded {
    if (name.contains("bundle") && name.contains("Release")) {
        doLast {
            val bundleFile = file("${project.buildDir}/outputs/bundle/release/ZebraEURED_SetupPrinter-release.aab")
            if (bundleFile.exists()) {
                val versionName = android.defaultConfig.versionName
                bundleFile.renameTo(file("${project.buildDir}/outputs/bundle/release/ZEURedBTFree-${versionName}-release.aab"))
            }
        }
    }
    if (name.contains("bundle") && name.contains("Debug")) {
        doLast {
            val bundleFile = file("${project.buildDir}/outputs/bundle/debug/ZebraEURED_SetupPrinter-debug.aab")
            if (bundleFile.exists()) {
                val versionName = android.defaultConfig.versionName
                bundleFile.renameTo(file("${project.buildDir}/outputs/bundle/debug/ZEURedBTFree-${versionName}-debug.aab"))
            }
        }
    }
}

dependencies {
    implementation(fileTree("libs") {
        include("*.jar", "*.aar")
    })
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Room Database
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)

    // ZXing barcode scanner
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Fragment navigation
    implementation("androidx.navigation:navigation-fragment:2.7.7")
    implementation("androidx.navigation:navigation-ui:2.7.7")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}