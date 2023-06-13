import org.xml.sax.Attributes
import org.xml.sax.helpers.DefaultHandler
import javax.xml.parsers.SAXParserFactory

group = "com.ixnah.hmcl"
version = "1.0-SNAPSHOT"

subprojects {
    tasks.withType<JavaCompile> {
        sourceCompatibility = "1.8"
        targetCompatibility = "1.8"

        options.encoding = "UTF-8"
    }
}

tasks.create("buildExecutableFile") {
    group = "build"
    doLast {
        val dirName = "buildExecutableFile"
        val tmpDir = temporaryDir.absoluteFile.also(File::mkdirs)
        var version = System.getProperty("hmcl_version", "");
        val baseUrl = "https://repo1.maven.org/maven2/org/glavo/hmcl/hmcl-dev"
        if (version.isBlank()) {
            val metadataURL = uri("$baseUrl/maven-metadata.xml").toURL()
            val latestReader = object: DefaultHandler() {
                val content = StringBuilder()
                var startRead = false

                override fun startElement(uri: String?, localName: String?, qName: String?, attributes: Attributes?) {
                    startRead = qName == "latest"
                }

                override fun characters(ch: CharArray?, start: Int, length: Int) {
                    if (startRead) {
                        content.append(ch!!.sliceArray(start until start+length))
                        startRead = false
                    }
                }

                override fun toString(): String {
                    return content.toString()
                }
            }
            SAXParserFactory.newInstance().newSAXParser().parse(metadataURL.openStream(), latestReader)
            version = latestReader.toString()
            if (version.isBlank()) {
                throw UnsupportedOperationException("can't get lastest version")
            }
        }
        val hmclFile = File(tmpDir, "HMCL-$version.jar").also {
            if (it.exists()) return@also
            val hmclUrl = uri("$baseUrl/$version/hmcl-dev-$version.jar").toURL()
            val tmpFile = File(tmpDir, "${System.currentTimeMillis()}.tmp")
            hmclUrl.openStream().copyTo(tmpFile.outputStream())
            tmpFile.copyTo(it, true)
            tmpFile.delete()
        }
        val execHeaderFile = File(tmpDir, "HMCLauncher.exe")
        val distDir = File(buildDir.absoluteFile, dirName).also(File::mkdirs)
        val output = File(distDir, hmclFile.nameWithoutExtension + ".exe")
        output.outputStream().use {
            it.write(execHeaderFile.readBytes())
            it.write(hmclFile.readBytes())
        }
    }
}

tasks.create("packageDistDir", Copy::class) {
    this.dependsOn(task("jar"), tasks["buildExecutableFile"])
    group = "build"
    val exeFileDir = File(buildDir.absoluteFile, "buildExecutableFile").also(File::mkdirs)
    val agentDir = File(projectDir.absoluteFile, "agent/build/libs").also(File::mkdirs)
    val exampleDir = File(projectDir.absoluteFile, "example/build/libs").also(File::mkdirs)
    from(exeFileDir)
    from(agentDir) {
        rename { "HMCLPluginLoader.jar" }
        into(".minecraft")
    }
    from(exampleDir) {
        into(".minecraft/plugins")
    }
    destinationDir = File(buildDir.absoluteFile, "dist").also(File::mkdirs)
}
