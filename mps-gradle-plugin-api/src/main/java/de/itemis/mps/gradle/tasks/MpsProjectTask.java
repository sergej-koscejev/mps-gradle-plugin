package de.itemis.mps.gradle.tasks;

import org.gradle.api.Incubating;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.logging.LogLevel;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Console;
import org.gradle.api.tasks.Internal;

/**
 * A Gradle task that operates on one or more MPS projects.
 *
 * <p>Use {@link org.gradle.api.tasks.TaskCollection#withType(Class)} to configure all MPS project tasks at once:
 *
 * <pre>{@code
 * tasks.withType(MpsProjectTask.class).configureEach(task -> {
 *     task.getMpsHome().set(layout.getProjectDirectory().dir("mps"));
 *     task.getProjectLocation().set(layout.getProjectDirectory().dir("my-mps-project"));
 *     task.getPluginRoots().from(task.getMpsHome().dir("plugins"));
 * });
 * }</pre>
 */
@Incubating
public interface MpsProjectTask extends MpsTask {
    /**
     * The MPS project directory. Convention: the Gradle project directory (but no convention is set on
     * multi-project tasks like MpsMigrate and Remigrate, so that the mutual exclusivity check with
     * {@code projectLocations} works correctly).
     */
    @Internal("too coarse to be used as input")
    DirectoryProperty getProjectLocation();

    /**
     * Root directories containing MPS plugins to load.
     */
    @Classpath
    ConfigurableFileCollection getPluginRoots();

    /**
     * Folder macros (path variables) to pass to MPS. Keys are macro names, values are directories.
     */
    @Internal("not considered input")
    MapProperty<String, Directory> getFolderMacros();

    /**
     * Log level for the MPS backend process.
     */
    @Console
    Property<LogLevel> getLogLevel();
}
