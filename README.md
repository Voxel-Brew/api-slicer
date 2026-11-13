# Slicer API

`dev.voxelbrew.api:slicer` publishes the MIT `dev.voxelbrew.api:slicer` artifact, including a
source JAR and `META-INF/LICENSE`. It owns the headless contracts in
`dev.voxelbrew.slicer.api`:

- `SlicerBackend`: version-2 backend SPI, input format, capabilities, probing and execution requests.
- `SlicerCatalog`: scoped discovery results and catalog factory.
- `Slicing`: validated job execution, publication guard, result and outdated-job exception.
- `SlicerRuntime`: runtime service factory used by CLI/GUI composition roots.

The module depends on `dev.voxelbrew.api:model` for CAD/manufacturing values and `dev.voxelbrew.api:export`
for the exporter catalog used to prepare slicer input. It has no dependency on
engine, concrete file I/O, JavaFX, picocli or Dagger. Process execution, built-in
backends, discovery and atomic job publication remain in the `impl/slicer` runtime.

## External backends

Compile against `dev.voxelbrew.api:slicer:<version>` with `compileOnly` (Maven
`provided`). Do not bundle host API classes in the provider JAR. Implement
`SlicerBackend` with a public no-argument constructor and register its class in:

```text
META-INF/services/dev.voxelbrew.slicer.api.SlicerBackend
```

Copy the JAR into the trusted plugin directory. `SlicerBackend.inputFormat()`
selects the exporter that prepares `Request.input()`; the backend writes only
its private supplied output. Honor cancellation and report actual probe capabilities.
The host validates and publishes the resulting job. The independent
`fixtures/exporter-plugin` build includes `consumer.ProbeSlicer` to verify that
providers compile against published APIs without application-module dependencies.
This fixture does not qualify a real slicer; real slicing tests use PrusaSlicer.

## Migration

Replace `dev.voxelbrew.exporter.printing.SlicerBackend` and
`dev.voxelbrew.exporter.api.SlicerCatalog` / `Slicing` imports with
`dev.voxelbrew.slicer.api` imports. Update the service-resource filename and
recompile providers. The SPI version remains 2 because its methods and semantics
are unchanged. `ExportRuntime` no longer provides slicing services; use
`SlicerRuntime` at the composition root and inject the individual services.
CLI/GUI include `slicer-api` at compile time and in their distributions while
keeping the concrete `:slicer` project as a runtime dependency.

```bash
./gradlew publishToMavenLocal
./gradlew -p ../../VoxelBrew :engine:buildExporterFixture :slicer:test --tests '*SlicerRuntimeTest' verifyFrontendIoDependencies
```

See [independent slicer configuration](https://github.com/Voxel-Brew/VoxelBrew/blob/master/docs/file-io.md#independent-slicer-configuration) for built-in options, persisted namespaces and plugin configuration hooks.

## Standalone build and publication

Java 21 and the committed Gradle wrapper are required. Run `./gradlew check`
(`.\gradlew.bat check` on Windows). This MIT library publishes as
`dev.voxelbrew.api:slicer:0.1.0-SNAPSHOT`; Java package names remain unchanged.
Use `./gradlew publishToMavenLocal` for local publication. Consumers opt in with
`-PuseMavenLocal=true`. `publishApiPublicationToGitHubPackagesRepository` publishes remotely.

GitHub Packages reads use `GITHUB_PACKAGES_USER` and `GITHUB_PACKAGES_TOKEN`
(a classic token with read:packages), or user Gradle properties
`githubPackagesUser` and `githubPackagesToken`. Publishing uses `GITHUB_ACTOR`
and `GITHUB_TOKEN`. Never store credentials in this repository.

The local workspace at `D:\repos\VoxelBrew` includes this checkout at `api/slicer`
and substitutes API dependencies with local sources. API versions are independent
of the application version. Pushes to master publish snapshots after checks;
pull requests only verify. Fork PRs use checked-out public API sources for dependencies.
