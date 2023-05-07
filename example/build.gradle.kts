plugins {
    id("java")
}

group = "com.ixnah.hmcl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":agent"))
}

tasks.jar {
    manifest {
        attributes(
            "Plugin-Class" to "com.ixnah.hmcl.plugin.ExamplePlugin",
            "Plugin-Id" to project.name,
            "Plugin-Version" to project.version,
            "Implementation-Version" to project.version,
        )
    }
}
