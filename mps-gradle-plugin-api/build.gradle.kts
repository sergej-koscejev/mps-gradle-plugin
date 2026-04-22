import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion

plugins {
    `kotlin-dsl`
    `maven-publish`
    alias(libs.plugins.kotlin.compatibility.validator)
}

group = "de.itemis.mps"

version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencyLocking {
    lockAllConfigurations()
}

publishing {
    repositories {
        val isSnapshot = project.version.toString().endsWith("SNAPSHOT")
        if (project.hasProperty("artifacts.itemis.cloud.user") && project.hasProperty("artifacts.itemis.cloud.pw")) {
            maven {
                name = "itemisCloud"
                if (isSnapshot) {
                    url = uri("https://artifacts.itemis.cloud/repository/maven-mps-snapshots/")
                } else {
                    url = uri("https://artifacts.itemis.cloud/repository/maven-mps-releases/")
                }
                credentials {
                    username = project.findProperty("artifacts.itemis.cloud.user") as String?
                    password = project.findProperty("artifacts.itemis.cloud.pw") as String?
                }
            }
        }

        if (!isSnapshot && project.hasProperty("gpr.token")) {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/mbeddr/mps-gradle-plugin")
                credentials {
                    username = project.findProperty("gpr.user") as String?
                    password = project.findProperty("gpr.token") as String?
                }
            }
        }
    }

    publications.withType<MavenPublication>().configureEach {
        versionMapping {
            allVariants {
                fromResolutionResult()
            }
        }
        pom {
            url = "https://github.com/mbeddr/mps-gradle-plugin"
            licenses {
                license {
                    name = "The Apache License, Version 2.0"
                    url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                }
            }
            scm {
                connection = "scm:git:git://github.com/mbeddr/mps-gradle-plugin.git"
                developerConnection = "scm:git:ssh://github.com/mbeddr/mps-gradle-plugin.git"
                url = "https://github.com/mbeddr/mps-gradle-plugin"
            }
        }
    }
}

java {
    targetCompatibility = JavaVersion.VERSION_11
    withSourcesJar()
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget(libs.versions.kotlinJvmTarget.get())
        apiVersion = KotlinVersion.fromVersion(libs.versions.kotlinApi.get())
        allWarningsAsErrors = true
    }
}

tasks.register("setTeamCityBuildNumber") {
    doLast {
        println("##teamcity[buildNumber '$version']")
    }
}
