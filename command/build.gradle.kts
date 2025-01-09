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

    compileOnlyApi(project(":paper"))

    // Lamp
    api("io.github.revxrsal:lamp.common:4.0.0-rc.2")
    api("io.github.revxrsal:lamp.bukkit:4.0.0-rc.2")
    api("io.github.revxrsal:lamp.brigadier:4.0.0-rc.2")
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