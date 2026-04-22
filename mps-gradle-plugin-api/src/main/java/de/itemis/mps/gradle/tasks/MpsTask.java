package de.itemis.mps.gradle.tasks;

import org.gradle.api.Incubating;
import org.gradle.api.Task;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.Nested;
import org.gradle.jvm.toolchain.JavaLauncher;

/**
 * A Gradle task that operates on an MPS installation.
 */
@Incubating
public interface MpsTask extends Task {
    /**
     * The MPS installation directory.
     */
    @Internal("not considered an input by itself")
    DirectoryProperty getMpsHome();

    /**
     * The MPS version string (e.g. "2021.3.3"). Detected automatically from {@link #getMpsHome()} if not set
     * explicitly.
     */
    @Input
    Property<String> getMpsVersion();

    /**
     * The Java launcher to use for running MPS. Each MPS version requires a specific Java version.
     */
    @Nested
    Property<JavaLauncher> getJavaLauncher();
}
