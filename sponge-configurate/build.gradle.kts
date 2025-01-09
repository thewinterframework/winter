plugins {
    id("java")
    `java-library`
}

repositories {
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.kryptonmc.org/releases")
    maven("https://repo.spongepowered.org/maven/")
}

dependencies {
    // Core
    api(project(":core"))
    annotationProcessor(project(":core"))

    // Configurate
    compileOnlyApi("org.spongepowered:configurate-yaml:4.2.0-SNAPSHOT")
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