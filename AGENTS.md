# Repository Guidelines

## Project Purpose
- This plugin adds IDE support for the Janitor scripting language (syntax highlighting and a built-in REPL tool window).
- The REPL runs inside the IDE and evaluates input via the Janitor runtime.

## Project Structure & Module Organization
- `src/main/kotlin/` holds the plugin implementation (services, startup activity, tool window).
- `src/main/resources/` contains plugin metadata and bundles, e.g. `META-INF/plugin.xml` and `messages/`.
- `src/test/kotlin/` contains tests; `src/test/testData/` stores fixture files used by tests.
- `src/main/antlr/Janitor.g4` is the ANTLR grammar source.
- Build configuration lives in `build.gradle.kts`, `gradle.properties`, and `gradle/`.
- The plugin description is pulled from `README.md` between `<!-- Plugin description -->` markers during the build.

## Build, Test, and Development Commands
- `./gradlew build` builds the plugin and runs checks.
- `./gradlew test` runs the JUnit-based test suite.
- `./gradlew runIde` starts an IntelliJ sandbox with the plugin installed.
- `./gradlew verifyPlugin` runs IntelliJ Platform plugin verification.
- `./gradlew qodana` (optional) runs static analysis using the Qodana config in `qodana.yml`.

## Coding Style & Naming Conventions
- Kotlin/Java standard style: 4-space indentation, no tabs, and newline at EOF.
- Use `UpperCamelCase` for classes and `lowerCamelCase` for functions/variables.
- Keep packages under `com.github.eischet.janitoridea` to match existing code.
- Prefer descriptive names for PSI/IDE actions (e.g., `MyToolWindowFactory`).

## Testing Guidelines
- Tests live in `src/test/kotlin/` and follow `*Test.kt` naming.
- Use fixtures from `src/test/testData/` for file-based tests (e.g., rename scenarios).
- Run `./gradlew test` before opening a PR; keep test data minimal and readable.

## Commit & Pull Request Guidelines
- Recent commits use short, imperative-style summaries (no prefix/type). Follow that pattern.
- PRs should include: a clear description, linked issues if applicable, and notes on how to verify (commands or screenshots for UI changes).

## Configuration & Release Notes
- Signing/publishing uses `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`, and `PUBLISH_TOKEN` env vars.
- Update `CHANGELOG.md` for user-visible changes; publishing runs `patchChangelog` automatically.
- Janitor runtime dependencies (e.g., `com.eischet.janitor:janitor-repl`) are resolved from `https://repo.srv.eischet.net/releases`.
