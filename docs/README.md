# Winter 3 documentation

These guides describe the `3.0.0` branch. The [GitHub wiki](https://github.com/thewinterframework/winter/wiki) mirrors them for browsing; the Markdown files in this directory are the versioned source.

Maintainers can copy these pages into a local wiki checkout with `pwsh scripts/sync-wiki.ps1 -WikiDirectory <wiki-checkout>`. The script adjusts relative links and creates the wiki Home and sidebar pages.

## Start here

Building a plugin with Codex? Install the public [`$winter-development` skill](https://github.com/thewinterframework/winter/tree/3.0.0/skills/winter-development) for Winter 3 Paper and Velocity projects.

1. [Getting started](getting-started.md): dependencies, boot class, and a first component.
2. [Configuration](configuration.md): compile-time `winter.properties`, diagnostics, and YAML files.
3. [Architecture](architecture.md): generated metadata and the runtime pipeline.

## Build a plugin

- [Components](components.md): discovery, stereotypes, metadata, ordering, and threads.
- [Lifecycle and modules](lifecycle.md): plugin phases, module dependencies, callbacks, and cleanup.
- [Dependency injection](dependency-injection.md): Guice bindings, providers, and qualifiers.
- [Exposed APIs and multi-module builds](expose-and-multi-module.md): `@Expose`, `@External`, and consumer wiring.
- [Conditions and decorators](conditions-and-decorators.md): conditional components and method annotations.
- [Scheduling](scheduling.md): scheduler API, timed methods, and tick conversion.
- [Paper integration](paper-and-yaml.md): listeners, YAML configuration, and platform behavior.
- [Velocity integration](velocity.md): proxy lifecycle, Guice, tasks, and plugin setup.
- [Text, expressions, and utilities](utilities.md): Adventure text, JEXL, and common helpers.

## Extend and maintain Winter

- [Extension points](extensions.md): handlers, interceptors, decorators, and annotation processors.
- [API map](api-reference.md): public entry points by package and generated artifacts.
- [Migration to 3.0](migration.md): changes from the legacy component model.
- [Troubleshooting](troubleshooting.md): build, class loading, injection, and thread issues.

For class and method signatures, build the Javadocs with `./gradlew :core:javadoc :paper:javadoc :velocity:javadoc` (or `gradlew.bat` on Windows). This documentation focuses on behavior, integration, and examples.
