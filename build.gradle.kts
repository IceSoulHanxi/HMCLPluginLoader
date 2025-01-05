group = "com.ixnah.hmcl"
version = "0.1"

subprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        options.encoding = "UTF-8"
    }
}

// TODO: makeExecutables
