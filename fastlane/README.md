fastlane documentation
----

# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```sh
xcode-select --install
```

For _fastlane_ installation instructions, see [Installing _fastlane_](https://docs.fastlane.tools/#installing-fastlane)

# Available Actions

## Android

### android test

```sh
[bundle exec] fastlane android test
```

Runs all the tests

### android testBuild

```sh
[bundle exec] fastlane android testBuild
```

Build app for production

### android productionBuild

```sh
[bundle exec] fastlane android productionBuild
```

Build app for production

### android qaBuild

```sh
[bundle exec] fastlane android qaBuild
```

Build app for QA testing and upload to GCS

### android testTeams

```sh
[bundle exec] fastlane android testTeams
```

Test teams workflow

### android beta

```sh
[bundle exec] fastlane android beta
```

Create Google Play Store bundle for production and upload to beta track

### android tests

```sh
[bundle exec] fastlane android tests
```

Run tests

----

This README.md is auto-generated and will be re-generated every time [_fastlane_](https://fastlane.tools) is run.

More information about _fastlane_ can be found on [fastlane.tools](https://fastlane.tools).

The documentation of _fastlane_ can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
