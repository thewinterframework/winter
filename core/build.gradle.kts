
plugins {
    id("java")
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.kryptonmc.org/releases")
}

dependencies {
    // UI/UX
    compileOnlyApi(libs.adventure)
    compileOnlyApi(libs.minimessage)
    compileOnlyApi(libs.kyori.text)
    compileOnlyApi(libs.kyori.legacy)

    // Dependency Injection
    api(libs.guice)

    // Common
    compileOnlyApi(libs.guava)

    // Logging
    compileOnlyApi("org.slf4j:slf4j-log4j12:1.7.29")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.add("-parameters")
}