# Development

This is a development focused supplement to [CONTRIBUTING.md](https://github.com/coditory/.github/blob/main/CONTRIBUTING.md).

## Commit messages
Before writing a commit message read [this article](https://chris.beams.io/posts/git-commit/).

## Build
Before pushing any changes make sure project builds without errors with:
`./gradlew build`

## Unit tests
This project uses [Spock](https://spockframework.org) for testing.
Please use the `Spec.groovy` suffix on new test classes.

Pull requests that lower test coverage will not be merged.
Test coverage metric will be visible on GitHub Pull request page.
It can be also generated in IDE or via command line with `./gradlew build coverage`
(generated report is in `build/report/jacoco/coverage/html`).

## Formatting
There are no enforced code style rules for Java and Groovy sources.
Just use IntelliJ code styles from "Project scheme" (`.idea/codeStyles`).

## Validate changes locally
Before submitting a pull request test your changes locally on a sample project.
You can test locally by publishing this library to maven local repository with
`./gradlew publishToMavenLocal`.

## Validating with snapshot release (optional)
Snapshot release is triggered manually by code owners.
To use a released snapshot version make sure to register Sonatype snapshot repository in gradle with:

```
// build.gradle.kts
repositories {
    mavenCentral()
    maven {
        url = URI("https://oss.sonatype.org/content/repositories/snapshots")
    }
}
```

## Documentation
If change adds a new feature or modifies existing one update README.