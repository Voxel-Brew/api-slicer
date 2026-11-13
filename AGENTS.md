# Slicer API instructions

- Own public slicer backend, catalog, execution and runtime-provider contracts.
- Depend only on api/model and api/export; never on AGPL application modules.
- Keep native handles, Protobuf, JavaFX, CLI parsing and DI annotations out of contracts.
- Keep concrete backends, discovery, process execution and file publication in VoxelBrew/slicer.
- Publish the MIT slicer-api artifact with source JAR and license. Verify independent consumer compilation.
- Document package and service-registration migrations in README.md and the application repository docs/file-io.md.

## Standalone repository

- Use Java 21 and the committed Gradle wrapper. Preserve the MIT license and public Java packages.
- Run `./gradlew spotlessApply` (Windows: `.\gradlew.bat spotlessApply`) before every commit, then review changes.
- Run `check` and validate Maven POM dependencies when changing publication configuration.
- Never add application-module dependencies or commit generated output or credentials.
