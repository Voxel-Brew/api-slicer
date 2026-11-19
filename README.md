# VoxelBrew Slicer API

`dev.voxelbrew.api:slicer` defines the MIT-licensed Java 21 contracts for slicer plugins. Its dependencies are the VoxelBrew BOM and model API.

A `SlicerProvider` exposes a stable `SlicerDescriptor` and creates a fresh `Slicer` for each operation. The host passes a detached, evaluated plate. The plugin collects settings with a `SlicerDialog<T>` through a `SlicerWindow`, invokes its backend, and returns a `PrintArtifact` with provenance. The host validates the artifact and owns attachment, saving, and printing.

Register providers in `META-INF/services/dev.voxelbrew.api.slicer.SlicerProvider`. The application supplies the `SlicerProviderRegistry` through Java's `ServiceLoader`. See the [provider example and lifecycle guide](docs/DEV_README.md).

## Breaking migration

The provider and operation contracts replace the previous slicer SPI. Update the provider implementation and service resource, then recompile plugins against the matching API. Application consumers must migrate with the API.

## Development

Run `./gradlew check` with Java 21, or `gradlew.bat check` on Windows. The parent workspace composite substitutes local BOM and model API sources. For a standalone build against Maven Local, publish the matching BOM and model API locally and pass `-PuseMavenLocal=true`. The checked-in release workflows manage snapshot and Maven Central publication.
