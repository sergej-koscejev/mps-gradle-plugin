package de.itemis.mps.gradle.tasks

import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.jvm.toolchain.JavaLauncher

/**
 * A Gradle task that operates on an MPS installation.
 */
@Incubating
interface MpsTask : Task {
    /**
     * The MPS installation directory.
     */
    val mpsHome: DirectoryProperty

    /**
     * The Java launcher to use for running MPS. Each MPS version requires a specific Java version.
     */
    val javaLauncher: Property<JavaLauncher>
}
