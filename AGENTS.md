# Repository Guidelines

## Project Structure & Module Organization

This repository publishes the headless `dev.voxelbrew.api:slicer` Java API. Public contracts live
under `src/main/java/dev/voxelbrew/slicer/api`; version metadata is in `src/main/resources`.
Gradle build logic is split between `build.gradle.kts` and `gradle/*.gradle.kts`, while quality
configuration lives in `config/quality`. Release automation is under `.github/scripts` and
`.github/workflows`.

Keep this module limited to slicer contracts, catalogs, and runtime access. Concrete backends,
process execution, file publication, UI, CLI parsing, and dependency-injection code belong in the
application repositories. Production dependencies must remain limited to the BOM plus the model
and export APIs.

## Build, Test, and Development Commands

- `./gradlew check` runs formatting checks, compilation, unit and integration tests, SpotBugs,
  dependency-boundary verification, and release-script tests.
- `./gradlew test` runs the JUnit 5 unit suite only.
- `./gradlew integrationTest` runs service integration tests; `./gradlew e2eTest` runs the separate
  end-to-end suite, which is not included in `check`.
- `./gradlew spotlessApply` formats Java, Kotlin Gradle scripts, Markdown, YAML, and properties.
- `./gradlew publishToMavenLocal` publishes a local snapshot for consumer testing. Use
  `-PuseMavenLocal=true` in consumers when required.

Java 21 and the committed Gradle wrapper are required. On Windows, use `gradlew.bat`.

## Coding Style & Naming Conventions

Spotless applies Palantir Java Format and ktfmt; do not hand-align code against those formatters.
Use UTF-8, four-space Java indentation, `PascalCase` for types, `camelCase` for methods and fields,
and `UPPER_SNAKE_CASE` for constants. Keep packages beneath `dev.voxelbrew.slicer.api`. Compilation
uses `-Xlint:all -Werror`, so new warnings must be resolved.

## Testing Guidelines

Place unit tests in `src/test/test/java`, integration tests in `src/test/integrationTest/java`, and
end-to-end tests in `src/test/e2eTest/java`; mirror these paths for resources. Name JUnit classes
`*Test` and cover contract validation, immutable-value behavior, and SPI compatibility. Run
`./gradlew check e2eTest` before submitting changes that affect runtime behavior.

## Commit & Pull Request Guidelines

History favors concise, imperative subjects, sometimes scoped with prefixes such as `docs:`,
`build:`, or `ci:`; milestone work may use `M4:`. Keep each commit focused. Pull requests should
explain the contract impact, identify linked issues, note compatibility or migration concerns, and
list verification commands. Include screenshots only for documentation rendering changes.

## Security & Publication

Never commit credentials, signing keys, generated artifacts, or local Gradle properties. Remote
publication is a release operation, not a validation step; test publication changes locally first.
