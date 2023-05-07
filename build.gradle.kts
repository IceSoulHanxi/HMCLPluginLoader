group = "com.ixnah.hmcl"
version = "1.0-SNAPSHOT"

subprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        options.encoding = "UTF-8"
    }
}
