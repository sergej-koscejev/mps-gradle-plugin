package de.itemis.mps.gradle.generate

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.PrintWriter
import java.util.*


abstract class FakeBuildNumberTask : DefaultTask() {

    @get:InputDirectory
    abstract val mpsDir: DirectoryProperty

    @TaskAction
    fun fakeBuildNumber() {
        val buildProperties = mpsDir.get().asFile.resolve("build.properties")

        if (!buildProperties.isFile) throw GradleException("can't locate build.properties file in MPS directory")

        val props = Properties()
        props.load(buildProperties.inputStream())
        val buildNumber = props.getProperty("mps.build.number")
        val buildTxt: Provider<RegularFile> = this.getBuildTxt()
        val writer = PrintWriter(buildTxt.get().asFile.outputStream())

        writer.write(buildNumber)
        writer.close()
    }

    @OutputFile
    fun getBuildTxt(): Provider<RegularFile> {
        return mpsDir.file("build.txt")
    }
}
