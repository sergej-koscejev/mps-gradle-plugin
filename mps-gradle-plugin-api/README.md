# mps-gradle-plugin-api

Task-type interfaces shared across the MPS task types provided by [`mps-gradle-plugin`](../mps-gradle-plugin).

## Coordinates

```kotlin
dependencies {
    compileOnly("de.itemis.mps:mps-gradle-plugin-api:1.0.0")
}
```

Requires Java 17.

## Interfaces

### `MpsTask`

Any task that operates on an MPS installation. Provides:

- `mpsHome: DirectoryProperty` — the MPS installation directory.
- `mpsVersion: Property<String>` — MPS version string (e.g. `2021.3.3`).
  Auto-detected from `mpsHome` when not set explicitly.
- `javaLauncher: Property<JavaLauncher>` — Java launcher used to run MPS.

### `MpsProjectTask`

Extends `MpsTask` for tasks that operate on one or more MPS projects. Provides:

- `projectLocation: DirectoryProperty`
- `pluginRoots: ConfigurableFileCollection`
- `folderMacros: MapProperty<String, Directory>`
- `logLevel: Property<LogLevel>`

## Bulk configuration

Use `tasks.withType` to configure every MPS task at once, for example to share
an MPS home and plugin roots across the whole build:

```kotlin
tasks.withType<MpsProjectTask>().configureEach {
    mpsHome = layout.projectDirectory.dir("mps")
    projectLocation = layout.projectDirectory.dir("my-mps-project")
    pluginRoots.from(mpsHome.dir("plugins"))
}
```
