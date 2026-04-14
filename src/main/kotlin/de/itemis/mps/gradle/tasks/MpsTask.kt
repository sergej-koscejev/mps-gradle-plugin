package de.itemis.mps.gradle.tasks

import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.UntrackedTask
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.work.DisableCachingByDefault

/**
 * A Gradle task that operates on an MPS installation.
 */
@Incubating
interface MpsTask : Task {
    /**
     * The MPS installation directory.
     */
    @get:Internal("not considered an input by itself")
    val mpsHome: DirectoryProperty

    /**
     * The MPS version string (e.g. "2021.3.3"). Detected automatically from [mpsHome] if not set explicitly.
     */
    @get:Input
    val mpsVersion: Property<String>

    /**
     * The Java launcher to use for running MPS. Each MPS version requires a specific Java version.
     *
     * Declared as a function rather than a Kotlin property to avoid a platform declarations clash
     * with [org.gradle.api.tasks.JavaExec.getJavaLauncher].
     */
    @Nested
    fun getJavaLauncher(): Property<JavaLauncher>
}
