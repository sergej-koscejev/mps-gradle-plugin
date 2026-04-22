package de.itemis.mps.gradle.tasks

import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Classpath
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Internal

/**
 * A Gradle task that operates on one or more MPS projects.
 *
 * Use [org.gradle.api.tasks.TaskCollection.withType] to configure all MPS project tasks at once:
 *
 * ```kotlin
 * tasks.withType<MpsProjectTask>().configureEach {
 *     mpsHome = layout.projectDirectory.dir("mps")
 *     projectLocation = layout.projectDirectory.dir("my-mps-project")
 *     pluginRoots.from(mpsHome.dir("plugins"))
 * }
 * ```
 */
@Incubating
interface MpsProjectTask : MpsTask {
    /**
     * The MPS project directory. Convention: the Gradle project directory (but no convention is set on
     * multi-project tasks like MpsMigrate and Remigrate, so that the mutual exclusivity check with
     * [projectLocations][MpsMigrate.projectLocations] works correctly).
     */
    @get:Internal("too coarse to be used as input")
    val projectLocation: DirectoryProperty

    /**
     * Root directories containing MPS plugins to load.
     */
    @get:Classpath
    val pluginRoots: ConfigurableFileCollection

    /**
     * Folder macros (path variables) to pass to MPS. Keys are macro names, values are directories.
     */
    @get:Internal("not considered input")
    val folderMacros: MapProperty<String, Directory>

    /**
     * Log level for the MPS backend process.
     */
    @get:Console
    val logLevel: Property<LogLevel>
}
