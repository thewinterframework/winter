plugins {
    id("java")
    `java-library`
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.kryptonmc.org/releases")
}

dependencies {
    // UI/UX
    compileOnlyApi(libs.adventure)
    compileOnlyApi(libs.minimessage)
    compileOnlyApi(libs.kyori.text)
    compileOnlyApi(libs.kyori.legacy)

    compileOnlyApi(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)

    // Dependency Injection
    api(libs.guice)

    // Common
    compileOnlyApi(libs.guava)

    // Logging
    compileOnlyApi("org.slf4j:slf4j-log4j12:1.7.29")

    // Expression Language
    implementation(libs.commons.jexl3)

    testImplementation(libs.junit.api)
    testImplementation(libs.adventure)
    testImplementation(libs.minimessage)
    testImplementation(libs.kyori.text)
    testImplementation(libs.kyori.legacy)
    testImplementation("org.slf4j:slf4j-api:1.7.29")
    testRuntimeOnly(libs.junit.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
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

tasks.test {
    useJUnitPlatform()
    systemProperty("winter.test.classpath", sourceSets.test.get().runtimeClasspath.asPath)
}
