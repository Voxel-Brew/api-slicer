# Slicer API instructions

- Own public slicer backend, catalog, execution and runtime-provider contracts.
- Keep printer profile catalogs and runtime access here; physical-device
  connections belong to api/printer.
- Depend only on api/model and api/export; never on AGPL application modules.
- Keep native handles, Protobuf, JavaFX, CLI parsing and DI annotations out of contracts.
- Keep concrete backends, discovery, process execution and file publication in VoxelBrew/slicer.
- Publish the MIT `dev.voxelbrew.api:slicer` artifact with source JAR and license. Verify independent consumer compilation.
- Document package and service-registration migrations in README.md and the application repository docs/file-io.md.

## Standalone repository

- Use Java 21 and the committed Gradle wrapper. Preserve the MIT license and public Java packages.
- Run `./gradlew spotlessApply` (Windows: `.\gradlew.bat spotlessApply`) before every commit, then review changes.
- Run `check` and validate Maven POM dependencies when changing publication configuration.
- Never add application-module dependencies or commit generated output or credentials.

## Development and verification

- Read `README.md` and the build before changing contracts or publication.
- Unit tests live in `src/test/test`; `integrationTest` covers service integration.
  `check` also runs formatting, dependency checks, SpotBugs and release-script
  tests; it does not automatically run the separate `e2eTest` task.
- API versions are independent of the application. Preserve version properties;
  local workspace composite builds substitute sibling API sources. Standalone
  consumers opt into Maven local with `-PuseMavenLocal=true` after publishing
  required dependencies.
- Instruction-only edits need Spotless and diff review. In a dirty checkout,
  scope formatting with `-PspotlessIdeHook=<comma-separated absolute paths>`.
- Remote publication is separate from validation; do not publish or change release
  versions merely to test a change.
