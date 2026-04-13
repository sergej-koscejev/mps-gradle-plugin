package de.itemis.mps.gradle.tasks

import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property

/**
 * A Gradle task that operates on one or more MPS projects.
 *
 * Use [org.gradle.api.tasks.TaskCollection.withType] to configure all MPS project tasks at once:
 *
 * ```kotlin
 * tasks.withType<MpsProjectTask>().configureEach {
 *     mpsHome.set(layout.projectDirectory.dir("mps"))
 *     projectLocation.set(layout.projectDirectory.dir("my-mps-project"))
 *     pluginRoots.from(layout.projectDirectory.dir("plugins"))
 * }
 * ```
 */
@Incubating
interface MpsProjectTask : MpsTask {
    /**
     * The MPS version string (e.g. "2021.3.3"). Detected automatically from [mpsHome] if not set explicitly.
     */
    val mpsVersion: Property<String>

    /**
     * The MPS project directory. Convention: the Gradle project directory (but no convention is set on
     * multi-project tasks like MpsMigrate and Remigrate, so that the mutual exclusivity check with
     * [projectLocations][MpsMigrate.projectLocations] works correctly).
     */
    val projectLocation: DirectoryProperty

    /**
     * Root directories containing MPS plugins to load.
     */
    val pluginRoots: ConfigurableFileCollection

    /**
     * Folder macros (path variables) to pass to MPS. Keys are macro names, values are directories.
     */
    val folderMacros: MapProperty<String, Directory>

    /**
     * Log level for the MPS backend process.
     */
    val logLevel: Property<LogLevel>
}
