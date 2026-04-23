package de.itemis.mps.gradle;

import de.itemis.mps.gradle.tasks.MpsTask
import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import org.gradle.jvm.toolchain.JavaLauncher
import org.gradle.process.ExecOperations
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

@DisableCachingByDefault(because = "Runs an Ant script that builds MPS languages and has non-trivial, external outputs")
abstract class RunAntScript : DefaultTask(), MpsTask {
    @Input
    lateinit var script: Any
    @Input
    var targets: List<String> = emptyList()
    @Optional @Classpath
    var scriptClasspath: FileCollection? = null
    @Input
    var scriptArgs: List<String> = emptyList()
    @Input
    var includeDefaultArgs = true
    @Input
    var includeDefaultClasspath = true
    @get:Inject
    protected abstract val execOperations: ExecOperations

    @Nested
    @Optional
    abstract override fun getJavaLauncher(): Property<JavaLauncher>

    /**
     * Whether to build incrementally.
     *
     * Possible values:
     * * `true` - perform an incremental build. If the [targets] list includes `clean` target it will be removed, and
     *   `-Dmps.generator.skipUnmodifiedModels=true` will be passed to Ant.
     * * `false` - The backwards compatible default. The [targets] list will not be modified and no properties will be
     *   passed to Ant. Any outside customizations made to targets and Ant arguments are left intact so the build may
     *   in fact be incremental.
     */
    @Input
    var incremental: Boolean = false

    fun targets(vararg targets: String) {
        this.targets = targets.toList()
    }

    @TaskAction
    fun build() {
        val allArgs = scriptArgs.toMutableList()
        if (includeDefaultArgs) {
            val defaultArgs = project.findProperty("itemis.mps.gradle.ant.defaultScriptArgs") as Collection<*>?
            if (defaultArgs != null) {
                allArgs += defaultArgs.map { it as String }
            }
        }

        val level = logLevel.get()
        if (level != LogLevel.LIFECYCLE && !allArgs.any { it.startsWith("-Dmps.ant.log=") }) {
            allArgs += "-Dmps.ant.log=${level.toString().lowercase()}"
        }

        if (incremental) {
            allArgs += "-Dmps.generator.skipUnmodifiedModels=true"
        }

        val mpsHomePath = mpsHome.get().asFile.absolutePath
        if (!allArgs.any { it.startsWith("-Dmps.home=") }) {
            allArgs += "-Dmps.home=$mpsHomePath"
        }
        if (!allArgs.any { it.startsWith("-Dmps_home=") }) {
            allArgs += "-Dmps_home=$mpsHomePath"
        }

        val targets = if (incremental) { targets - "clean" } else { targets }

        val effectiveExecutable = javaLauncher.orNull?.executablePath?.asFile
            ?: project.findProperty("itemis.mps.gradle.ant.defaultJavaExecutable")

        val effectiveClasspath: FileCollection = scriptClasspath ?: mpsHome.asFileTree.matching {
            include("lib/ant/lib/*.jar")
            include("lib/*.jar")
        }

        execOperations.javaexec {
            if (effectiveExecutable != null) {
                executable(effectiveExecutable)
            }

            mainClass.set("org.apache.tools.ant.launch.Launcher")
            workingDir = project.rootDir

            if (includeDefaultClasspath) {
                val defaultClasspath = project.findProperty("itemis.mps.gradle.ant.defaultScriptClasspath")
                if (defaultClasspath != null) {
                    classpath(defaultClasspath)
                }
            }

            classpath(effectiveClasspath)

            args(allArgs + "-buildfile" + project.file(script).toString() + targets)
        }
    }
}

@DisableCachingByDefault(because = "Runs an Ant script that builds MPS languages and has non-trivial, external outputs")
abstract class BuildLanguages : RunAntScript() {
    init {
        targets = listOf("clean", "generate", "assemble")
    }
}

@DisableCachingByDefault(because = "Runs an Ant script that tests MPS languages and has non-trivial, external outputs")
abstract class TestLanguages : RunAntScript() {
    init {
        targets = listOf("clean", "generate", "assemble", "check")
    }
}
