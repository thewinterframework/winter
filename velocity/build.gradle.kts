plugins {
  id("java")
  `java-library`
}

repositories {
  maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
  compileOnlyApi(libs.velocity)

  api(project(":core"))
  annotationProcessor(project(":core"))

  // Velocity generates velocity-plugin.json from the consumer's @Plugin declaration.
  annotationProcessor(libs.velocity)

  testImplementation(libs.junit.api)
  testImplementation(libs.velocity)
  testRuntimeOnly(libs.junit.engine)
  testAnnotationProcessor(project(":core"))
  testAnnotationProcessor(libs.velocity)
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
}
