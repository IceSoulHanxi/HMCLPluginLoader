plugins {
    id("java")
}

group = "com.ixnah.hmcl"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly(project(":loader"))
    compileOnly("org.glavo.hmcl:hmcl-dev:3.6.11.264")
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
