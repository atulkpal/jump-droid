# Contributing to Jump Droid

Thank you for your interest in contributing! Jump Droid is an open-source project, and contributions are welcome.

## Getting Started

1. Fork the repository.
2. Create a branch following our [branch policy](AGENTS.md#14-git-branch-policy).
3. Make your changes.
4. Open a pull request.

## Before You Code

- **Gameplay content** (threats, platforms, zones, power-ups, lore, artifacts, rockets): Check the [Design Libraries](docs/design/) first. All content must originate from approved design entries. See [AGENTS.md §15](AGENTS.md#15-design-library-first-rule).
- **Bug fixes**: Open an issue first, then link your PR to it.
- **Architecture changes**: Discuss in a GitHub issue before implementing.

## Code Style

- Follow existing patterns in the codebase.
- Kotlin conventions per JetBrains style guide.
- No commented-out code.
- Keep `GameScreen.kt` under 2,200 lines.

## Pull Request Process

1. Ensure your branch is up to date with `master`.
2. Test your changes: `./gradlew assembleDebug` must pass.
3. Update documentation (CHANGELOG, design libraries, etc.) if applicable.
4. PR title format: `type: brief description` (e.g., `fix: prevent MusicPlayer crash on game over`).

## Code of Conduct

Be respectful and constructive. This is a learning and community project.
