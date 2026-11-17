# Slicer API

`dev.voxelbrew.api:slicer` publishes the MIT `dev.voxelbrew.api:slicer` artifact, including a
source JAR and `META-INF/LICENSE`. It owns the headless contracts in
`dev.voxelbrew.api.slicer`:

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
META-INF/services/dev.voxelbrew.api.slicer.SlicerBackend
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
`dev.voxelbrew.api.slicer` imports. Update the service-resource filename and
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
`dev.voxelbrew.api:slicer:0.1.0-SNAPSHOT`.
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

## Maven Central releases

Release coordinates are `dev.voxelbrew.api:ARTIFACT:VERSION`, under the verified
`dev.voxelbrew` namespace. Snapshots remain in GitHub Packages; releases resolve
from Maven Central without GitHub credentials. Local `publishToMavenLocal` remains
available without signing credentials.

### GitHub Actions

1. Run **Prepare release** (`release-prepare.yml`) on `master`. It creates
   `release/A.B.x`, pins the BOM to an exact release, and advances master
   to the next minor snapshot using an atomic push. Optional `bom-version` selects the released BOM. Otherwise the current BOM
   version without `-SNAPSHOT` is used; that release must exist in Central before
   publishing this API.
2. Run **Publish to prod** (`release.yml`) on that release branch with the default
   `dry-run: true`. This checks and builds a local release candidate without
   changing remote refs or publishing. It resolves real released dependencies,
   not composite source substitutions.
3. Run again with `dry-run: false`. The workflow validates signing credentials,
   signs the candidate, reserves immutable tag `vA.B.C`, and publishes to the
   Central Portal. Only after Central reports `PUBLISHED` does it create the
   GitHub release and advance the release branch to the next patch snapshot.

Publish a release BOM to Maven Central before releasing APIs that import it.
The BOM contains constraints only, so it can precede its managed artifacts.
For the first API release, publish **model**, then **export/import/printer**, then
**slicer**. Slicer depends on model and export. Allow Central's public repository
to propagate each dependency before releasing its consumers. API versions remain
independent of application versions.

Configure these repository or organization secrets (grant organization secrets
access to each API repository):

| Secret                    | Value                                                         |
| ------------------------- | ------------------------------------------------------------- |
| `MAVEN_CENTRAL_USERNAME`  | Central Portal token username, not account login              |
| `MAVEN_CENTRAL_PASSWORD`  | Central Portal token password                                 |
| `SIGNING_KEY_ID`          | Signing key ID (short ID for Gradle's legacy key-ring reader) |
| `SIGNING_KEY_RING_BASE64` | Base64-encoded exported secret key ring                       |
| `SIGNING_PASSWORD`        | Signing key passphrase; omit for an unprotected key           |

The public signing key must be distributed as required by Central and must not
be expired or revoked. A Portal token authenticates uploads; it does not replace
the required PGP signatures. Keys are decoded only into the temporary runner
directory and removed in an always-run cleanup step. No secrets belong in Git.

The build supplies binary, sources and Javadoc JARs, checksums, signatures and POM
metadata (license, developers and SCM). It rejects snapshots and dynamic direct
dependencies for Central releases. `-PcentralRelease=true` explicitly enables
signing and Central upload. For local publishing, use `SIGNING_KEY_ID`,
`SIGNING_PASSWORD`, `SIGNING_KEY_RING` (file path), and Gradle environment variables
`ORG_GRADLE_PROJECT_mavenCentralUsername` / `ORG_GRADLE_PROJECT_mavenCentralPassword`.

### Recovery

If signing or verification fails, no release tag is pushed. If an upload fails,
the reserved tag remains and the branch stays at its original snapshot. Rerun
on the unchanged branch to reuse exactly that tag's candidate; a different tree
or source commit is rejected. Never delete or overwrite a published version.

If Central already published but the workflow failed before advancing the branch,
do not upload again. Check out the same source, run `node .github/scripts/release.cjs
candidate` with the original workflow's `GITHUB_REF_NAME`, `GITHUB_SHA` and
`GITHUB_REPOSITORY`, verify the existing Central release, then run `node
.github/scripts/release.cjs finish` with an authorized `GH_TOKEN`. This final step
creates the GitHub release if absent and advances the branch. Port release fixes
back to master through normal reviewed changes; do not merge release version
bumps into master.

References: [Central requirements](https://central.sonatype.org/publish/requirements/)
and [Gradle Central publishing plugin](https://vanniktech.github.io/gradle-maven-publish-plugin/central/).

## BOM alignment

This build imports `dev.voxelbrew:bom` using `bomVersion` in `gradle.properties`.
API dependencies omit versions; `projectVersion` remains this API's own release version.
The BOM is metadata only and adds no application libraries.
For local development, publish `../../BOM` to Maven local first and pass
`-PuseMavenLocal=true`, or use the parent workspace composite.
Standalone builds require the selected BOM to be published before use.

## Package migration

Public contracts now live under `dev.voxelbrew.api.slicer`.
This is a source and binary incompatible package rename. Update imports and service
registration filenames, then recompile consumers and plugin providers against the
matching model, import, export, printer, and slicer API snapshots. Maven coordinates
and release versions are unchanged; deploy the matching application and APIs together.
