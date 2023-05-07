plugins {
    id("java")
    id("java-library")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

group = "com.ixnah.hmcl"

repositories {
    mavenCentral()
}

dependencies {
    api("org.pf4j:pf4j:3.9.0")
    api("org.slf4j:slf4j-api:2.0.7")
    api("org.ow2.asm:asm-tree:9.5")
    compileOnly("org.jetbrains:annotations:24.0.1")
    compileOnly("org.glavo.hmcl:hmcl-dev:3.5.4.232")
}

tasks.jar {
    enabled = false
    dependsOn(tasks["shadowJar"])
}

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set(null as String?)
    manifest {
        attributes(
            "Premain-Class" to "com.ixnah.hmcl.agent.AgentMain",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Implementation-Version" to project.version,
        )
    }
}
