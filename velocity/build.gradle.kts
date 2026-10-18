plugins {
    id("java")
    `java-library`
}

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnlyApi(libs.velocity)
    api(project(":core"))
    annotationProcessor(project(":core"))

    compileOnlyApi(libs.auto.service.annotations)
    annotationProcessor(libs.auto.service)
    implementation(libs.snakeyaml)

    testImplementation(libs.velocity)
    testImplementation(libs.junit.api)
    testImplementation(libs.adventure)
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
