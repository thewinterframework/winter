plugins {
    id("java")
    id("com.github.johnrengelman.shadow") version ("8.0.0")
    `java-library`
}

repositories {
    mavenLocal()
    mavenCentral()

    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.kryptonmc.org/releases")
}

dependencies {
    api(project(":paper"))
    annotationProcessor(project(":paper"))

    api(project(":sponge-configurate"))
    annotationProcessor(project(":sponge-configurate"))

    api(project(":command"))
    annotationProcessor(project(":command"))
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}