package de.itemis.mps.gradle.tasks

import de.itemis.mps.gradle.BackendConfigurations
import de.itemis.mps.gradle.TaskGroups
import de.itemis.mps.gradle.launcher.MpsBackendBuilder
import de.itemis.mps.gradle.launcher.MpsVersionDetection
import org.gradle.api.GradleException
import org.gradle.api.Incubating
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.logging.LogLevel
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.mapProperty
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty
import org.gradle.process.CommandLineArgumentProvider
import javax.inject.Inject

@Incubating
@UntrackedTask(because = "Operates 'in place'")
open class Remigrate @Inject constructor(
    private val objectFactory: ObjectFactory,
    providerFactory: ProviderFactory
) : JavaExec(), MpsProjectTask {

    @get:Input
    override val logLevel: Property<LogLevel> = objectFactory.property<LogLevel>().convention(project.gradle.startParameter.logLevel)

    @get:Internal("covered by mpsVersion and classpath")
    override val mpsHome: DirectoryProperty = objectFactory.directoryProperty()

    @get:Input
    @get:Optional
    override val mpsVersion: Property<String> = objectFactory.property<String>()
        .convention(MpsVersionDetection.fromMpsHome(project.layout, providerFactory, mpsHome.asFile))

    @get:Internal("covered by allProjectFiles")
    override val projectLocation: DirectoryProperty = objectFactory.directoryProperty()

    @get:Internal("covered by allProjectFiles")
    val projectLocations: ConfigurableFileCollection = objectFactory.fileCollection()

    @get:InputFiles
    @get:SkipWhenEmpty
    @get:IgnoreEmptyDirectories
    protected val allProjectFiles = providerFactory.provider {
        effectiveProjectLocations().flatMap { objectFactory.fileTree().from(it) }
    }

    @get:Internal("Folder macros are ignored for the purposes of up-to-date checks and caching")
    override val folderMacros: MapProperty<String, Directory> = objectFactory.mapProperty()

    @get:Classpath
    override val pluginRoots: ConfigurableFileCollection = objectFactory.fileCollection()

    @get:Internal
    val additionalClasspath: ConfigurableFileCollection =
        objectFactory.fileCollection().from(initialBackendClasspath())

    @get:Input
    val excludedModuleMigrations: SetProperty<ExcludedModuleMigration> = objectFactory.setProperty()

    fun excludeModuleMigration(language: String, version: Int) {
        excludedModuleMigrations.add(ExcludedModuleMigration(language, version))
    }

    init {
        val backendBuilder: MpsBackendBuilder = project.objects.newInstance(MpsBackendBuilder::class)
        backendBuilder.withMpsHomeDirectory(mpsHome).withMpsVersion(mpsVersion).configure(this)

        val backendConfig = project.configurations.named(BackendConfigurations.REMIGRATE_BACKEND_CONFIGURATION_NAME)
        dependsOn(backendConfig)

        argumentProviders.add(CommandLineArgumentProvider {
            val result = mutableListOf<String>()

            for (dir in effectiveProjectLocations()) {
                result.add("--project=$dir")
            }

            addPluginRoots(result, pluginRoots)

            if (logLevel.get() <= LogLevel.INFO) {
                result.add("--log-level=${logLevel.get()}")
            }

            addFolderMacros(result, folderMacros)

            val pluginFile = backendConfig.get().resolvedConfiguration.firstLevelModuleDependencies
                .flatMap { it.moduleArtifacts.map { it.file } }
                .single()

            result.add("--plugin=de.itemis.mps.buildbackends.remigrate::$pluginFile")

            result.addAll(
                excludedModuleMigrations.get().map { "--exclude-module-migration=${it.language}:${it.version}" })

            result
        })

        group = TaskGroups.MIGRATION

        // Additional classpath goes before backend config in order to fix problem with Kotlin version mismatch of
        // backend vs IDEA.
        classpath(additionalClasspath)
        classpath(backendConfig)

        mainClass.set("de.itemis.mps.gradle.remigrate.MainKt")
    }

    @TaskAction
    override fun exec() {
        for (dir in effectiveProjectLocations()) {
            checkProjectLocation(dir)
        }
        super.exec()
    }

    private fun effectiveProjectLocations(): FileCollection {
        val hasMultiple = !projectLocations.isEmpty
        val hasSingle = projectLocation.isPresent
        if (hasMultiple && hasSingle) {
            throw GradleException("Cannot set both projectLocation and projectLocations. Use one or the other.")
        }
        if (!hasMultiple && !hasSingle) {
            throw GradleException("Must set either projectLocation or projectLocations.")
        }
        return if (hasMultiple) projectLocations else objectFactory.fileCollection().from(projectLocation)
    }

    private fun initialBackendClasspath() = mpsHome.asFileTree.matching {
        include("lib/**/*.jar")
    }
}
