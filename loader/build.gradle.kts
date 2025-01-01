plugins {
    id("java")
    id("java-library")
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

group = "com.ixnah.hmcl"

repositories {
    mavenCentral()
}

dependencies {
    api("org.pf4j:pf4j:3.13.0")
    api("org.slf4j:slf4j-api:2.0.16")
    api("org.ow2.asm:asm-tree:9.7.1")
}

tasks.jar {
    enabled = false
    dependsOn(tasks["shadowJar"])
}

tasks.getByName<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set(null as String?)
    manifest {
        attributes(
            "Main-Class" to "com.ixnah.hmcl.Main",
            "Premain-Class" to "com.ixnah.hmcl.agent.AgentMain",
            "Can-Redefine-Classes" to "true",
            "Can-Retransform-Classes" to "true",
            "Implementation-Version" to project.version,
            "Class-Path" to "pack200.jar",
            "Add-Opens" to listOf(
                "java.base/java.lang",
                "java.base/java.lang.reflect",
                "java.base/jdk.internal.loader",
                "javafx.base/com.sun.javafx.binding",
                "javafx.base/com.sun.javafx.event",
                "javafx.base/com.sun.javafx.runtime",
                "javafx.graphics/javafx.css",
                "javafx.graphics/com.sun.javafx.stage",
                "javafx.graphics/com.sun.prism",
                "javafx.controls/com.sun.javafx.scene.control",
                "javafx.controls/com.sun.javafx.scene.control.behavior",
                "javafx.controls/javafx.scene.control.skin",
                "jdk.attach/sun.tools.attach"
            ).joinToString(" ")
        )
    }
}
