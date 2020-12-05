import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm")
    application
}

group = "io.github.commandertvis"
version = "0.0.1"

repositories {
    jcenter()
    maven("http://zoidberg.ukp.informatik.tu-darmstadt.de/artifactory/public-releases/")
}

dependencies {
    implementation("gnu.trove:trove:3.0.3")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation(kotlin("test-junit5"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }

    test.get().useJUnitPlatform()
}

application.mainClassName = "MainKt"

kotlin.sourceSets.all {
    languageSettings.apply {
        useExperimentalAnnotation("kotlin.time.ExperimentalTime")
        enableLanguageFeature("InlineClasses")
    }
}
