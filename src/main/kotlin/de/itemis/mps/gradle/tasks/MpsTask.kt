package de.itemis.mps.gradle.tasks

import org.gradle.api.Incubating
import org.gradle.api.Task
import org.gradle.api.file.DirectoryProperty

/**
 * A Gradle task that operates on an MPS installation.
 */
@Incubating
interface MpsTask : Task {
    /**
     * The MPS installation directory.
     */
    val mpsHome: DirectoryProperty
}
