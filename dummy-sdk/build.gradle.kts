import java.io.FileInputStream
import java.net.URI
import java.util.Properties

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

val localProperties = Properties().apply {
    load(FileInputStream(File(rootProject.rootDir, "local.properties")))
}

val getReleaseTagSuffix = System.getenv("RELEASE_TAG_SUFFIX")?.run { "" }

val getVersionName = "1.0.0"

val getVersionNameForMavenPublishing = "${getVersionName}${getReleaseTagSuffix}"

val getArtifactId = "muses"

val getGroupId = "com.lazypay.android"

val getReleaseVariant = System.getenv("RELEASE_VARIANT") ?: "debug"


android {
    namespace = "com.kunalapk.dummy_sdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
}

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = getGroupId
            artifactId = artifactId
            version = getVersionNameForMavenPublishing
            artifact("$buildDir/outputs/aar/${getArtifactId}-${getVersionName}-${getReleaseVariant}.aar")
            pom.withXml {
                val dependenciesNode = asNode().appendNode("dependencies")
                configurations.implementation.get().allDependencies.forEach {
                    val dependencyNode = dependenciesNode.appendNode("dependency")
                    dependencyNode.appendNode("groupId", it.group)
                    dependencyNode.appendNode("artifactId", it.name)
                    dependencyNode.appendNode("version", it.version)
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/paysense-india-services/${getArtifactId}")
            credentials {
                username = localProperties["gpr.usr"]?.run { System.getenv("GPR_USER") }
                password = localProperties["gpr.key"]?.run { System.getenv("GPR_API_KEY") }
            }
        }
        maven {
            url = URI("${System.getProperty("user.home")}/maven_local_repo")
        }
    }
}


tasks.register("createTag") {
    doLast {
        val tagName = "v${getVersionNameForMavenPublishing}"
        createReleaseTag(tagName)
    }
}

fun createReleaseTag(tagName: String) {
    runCommands(listOf("git", "tag", "-a", tagName, "-m", tagName))
    runCommands(listOf("git", "push", "origin", tagName))
}

fun runCommands(commands: List<String>): String {
    val process = ProcessBuilder(commands).redirectErrorStream(true).start()
    process.waitFor()
    var result = ""
    process.inputStream.reader().forEachLine { result += it + '\n' }
    val errorResult = process.exitValue() == 0
    if (!errorResult) {
        throw IllegalStateException(result)
    }
    return result
}