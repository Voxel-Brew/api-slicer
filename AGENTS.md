# Repository Guidelines

## Project Structure & Module Organization

This repository publishes the headless `dev.voxelbrew.api:slicer` Java API. Public contracts live
under `src/main/java/dev/voxelbrew/api/slicer`; version metadata is in `src/main/resources`.
Gradle build logic is split between `build.gradle.kts` and `gradle/*.gradle.kts`, while SpotBugs
configuration comes from the public BOM master branch. Release automation is under `.github/scripts` and
`.github/workflows`.

Keep this module limited to slicer provider and operation contracts. Concrete backends,
process execution, file publication, UI, CLI parsing, and dependency-injection code belong in the
application repositories. Production dependencies must remain limited to the BOM and model API.

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

Run `./gradlew checkBinaryCompatibility` to compare the built JAR with the latest stable Maven
Central release in the same major at or below `projectVersion` (ignoring `-SNAPSHOT`). The
`check` task includes this comparison on pull requests and pushes to master and release branches.

## Coding Style & Naming Conventions

Spotless applies Palantir Java Format and ktfmt; do not hand-align code against those formatters.
Use UTF-8, four-space Java indentation, `PascalCase` for types, `camelCase` for methods and fields,
and `UPPER_SNAKE_CASE` for constants. Keep packages beneath `dev.voxelbrew.api.slicer`. Compilation
uses `-Xlint:all -Werror`, so new warnings must be resolved.

Run `./gradlew checkJavaMemberOrder` to check Java source declaration order. The `check` and
`build` tasks include it. Within each class, start with static final fields, then other static fields,
final instance fields, other instance fields, static nested types, inner classes, static methods,
constructors, and instance methods. Preserve Java initialization order when moving fields.

## Testing Guidelines

Place unit tests in `src/test/test/java`, integration tests in `src/test/integrationTest/java`, and
end-to-end tests in `src/test/e2eTest/java`; mirror these paths for resources. Name JUnit classes
`*Test` and cover contract validation, immutable-value behavior, and SPI compatibility. Run
`./gradlew check e2eTest` before submitting changes that affect runtime behavior.

## Pre-commit SpotBugs

The build resolves VoxelBrew artifacts through Maven and fetches shared quality policy
from the public BOM master branch.

Before committing, run `./gradlew spotbugsMain` from this API repository or
`./gradlew :slicer:spotbugsMain` from the composite workspace.
Use `.\gradlew.bat` in place of `./gradlew` on Windows.

## Commit & Pull Request Guidelines

Prefer **Rebase and merge** for pull requests. Use **Squash and merge** only as an exception.
The **Create a merge commit** option is blocked.

History favors concise, imperative subjects, sometimes scoped with prefixes such as `docs:`,
`build:`, or `ci:`; milestone work may use `M4:`. Keep each commit focused. Pull requests should
explain the contract impact, identify linked issues, note compatibility or migration concerns, and
list verification commands. Include screenshots only for documentation rendering changes.

When a commit addresses a known GitHub issue, include the issue number in the
commit subject as the scope, for example `feat(#1337): foo boo bar baz`.

When a pull request resolves a known GitHub issue, add `Closes #123` to the PR description,
using the actual issue number. For an issue in another repository, use
`Closes owner/repository#123`. Add the closing reference to the PR targeting
`master` so GitHub closes the issue when the PR is merged.

## Security & Publication

Never commit credentials, signing keys, generated artifacts, or local Gradle properties. Remote
publication is a release operation, not a validation step; test publication changes locally first.

## Lombok in Java code

Use Lombok where it removes routine Java boilerplate without obscuring a public contract. Apply these annotations in Java-bearing repositories and modules; the website has no Java production code.

- `@Getter` / `@Setter`: Generate straightforward accessors for fields that should be exposed or mutable. Prefer field-level annotations when only some fields qualify; retain custom accessors that enforce invariants or make defensive copies.
- `@RequiredArgsConstructor` / `@NoArgsConstructor`: Generate constructors for required final or `@NonNull` fields, or a no-argument constructor when a framework or serializer needs one. Keep explicit constructors when they validate, transform, or document special initialization behavior.
- `@NonNull`: Mark required fields, parameters, and other supported elements to express and enforce non-null inputs. Replace equivalent `Objects.requireNonNull` checks in those places; keep explicit checks when they provide distinct behavior or messages.
- `@ToString`: Generate useful diagnostic string representations for value or state classes. Exclude secrets, large graphs, and recursive relationships.
- `@Data`: Use for mutable data holders that genuinely need getters, setters, equality, hashing, and string representation together. Prefer narrower annotations when only some behavior is appropriate.
- `@Value`: Use for immutable value types when its generated constructor, accessors, equality, hashing, and string representation fit the contract. Preserve defensive copies for mutable components.
- `@EqualsAndHashCode`: Generate value equality for classes whose identity is defined by selected fields. Choose included fields and superclass handling deliberately; avoid mutable fields that would break hash collections.
- `@Slf4j`: Add a logger to classes that emit operational diagnostics. Use parameterized logging and keep credentials and other sensitive data out of logs.
- `@Builder`: Use for objects with many optional or named construction inputs when it improves call-site clarity. Keep required-field validation and invariants explicit.
- `@UtilityClass`: Use for stateless collections of static helpers that must not be instantiated. Do not use it for classes that may need instances, inheritance, or dependency injection.

## AssertJ in Java tests

Use AssertJ (org.assertj:assertj-core) for assertions in Java unit, integration, and end-to-end tests. Keep JUnit Jupiter for test lifecycle, parameterization, and execution. Declare AssertJ without a version in test dependencies; the BOM manages its version. Prefer ssertThat, ssertThatThrownBy, and ssertThatCode with descriptive failure messages and appropriate numeric tolerances. Do not add new JUnit Assertions calls.

## Service injection

Resolve services by their interfaces at the wiring point and pass them to consumers through
constructors. Provider classes must not expose static service getters or downcast discovered
services to their implementations.

## Release workflows

Dispatch **Prepare release** on `master` to cut `release/A.B.x` and open the
next-minor snapshot pull request. Merge that PR through normal branch protection.
Dispatch `release.yml` on the release branch, first with the default dry run,
then with the dry run disabled to sign, reserve annotated tag `vA.B.C`, publish
to Maven Central, create the GitHub release and advance the patch snapshot.

Release-branch pushes and completed publications run `release-merge-back.yml`.
It proposes release fixes in a PR against master, retaining master's
`projectVersion`. Resolve other conflicts through normal review. To retry,
dispatch merge-back on the release branch.

Allow GitHub Actions to create pull requests, or configure `RELEASE_PR_TOKEN`
with contents and pull-request write access for the automation branches. Use a
dedicated token when branch protection requires CI triggered by PR updates.
