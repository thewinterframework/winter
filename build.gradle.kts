plugins {
    id("java")
    id("maven-publish")
    id("io.deepmedia.tools.deployer") version "0.16.0"
    signing
}

subprojects {
    apply(plugin = "java")
    apply(plugin = "io.deepmedia.tools.deployer")

    java {
        withJavadocJar()
        withSourcesJar()
    }

    deployer {
        signing {
            key.set(secret("winterSigningKey"))
            password.set(secret("winterSigningPassphrase"))
        }

        content {
            component {
                fromJava()
            }
        }

        projectInfo {
            description.set("The framework to make plugin creation easier than ever.")
            url.set("https://github.com/thewinterframework/winter")
            license(MIT)

            groupId.set(group.toString())
            scm.fromGithub("thewinterframework", "winter")
            developer("Diego Cardenas", "diego.cardenas.v06@gmail.com")
            artifactId.set(project.name)
        }

        centralPortalSpec {
            signing.key.set(secret("winterSigningKey"))
            signing.password.set(secret("winterSigningPassphrase"))
            auth.user.set(secret("winterRepositoryUsername"))
            auth.password.set(secret("winterRepositoryPassword"))
        }
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}
