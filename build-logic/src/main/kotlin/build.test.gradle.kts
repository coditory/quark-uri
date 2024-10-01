import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("java-library")
    id("groovy")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
    testLogging {
        events =
            setOf(
                TestLogEvent.FAILED,
                TestLogEvent.STANDARD_ERROR,
                TestLogEvent.STANDARD_OUT,
                TestLogEvent.SKIPPED,
                TestLogEvent.PASSED,
            )
        exceptionFormat = TestExceptionFormat.FULL
        showExceptions = true
        showCauses = true
        showStackTraces = true
    }
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks.withType<GroovyCompile>().configureEach {
    groovyOptions.encoding = "UTF-8"
}
