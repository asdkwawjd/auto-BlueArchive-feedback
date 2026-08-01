plugins {
    id("java")
    application
}

group = "jp.bluearchive.shit.autofeedback"
version = "1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "jp.bluearchive.shit.autofeedback.Main"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.seleniumhq.selenium:selenium-java:4.27.0")
    implementation("io.github.bonigarcia:webdrivermanager:6.1.0")
    implementation("com.github.kwhat:jnativehook:2.2.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.2")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.register<Jar>("fatJar") {
    group = "build"
    description = "Builds an executable JAR containing all runtime dependencies."
    archiveClassifier = "all"
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = application.mainClass.get()
    }

    from(sourceSets.main.get().output)
    dependsOn(configurations.runtimeClasspath)
    from({
        configurations.runtimeClasspath.get().map { dependency ->
            if (dependency.isDirectory) dependency else zipTree(dependency)
        }
    }) {
        exclude("META-INF/*.SF", "META-INF/*.DSA", "META-INF/*.RSA")
    }
}

tasks.register<Sync>("jarDistribution") {
    group = "distribution"
    description = "Collects the executable JAR and its input text files."
    dependsOn("fatJar")
    from(tasks.named<Jar>("fatJar").flatMap { it.archiveFile })
    from("content.txt", "emails.txt")
    into(layout.buildDirectory.dir("distributions/jar"))
}

val portableAppName = "BlueArchiveFeedback"
val portableVersion = "1.0.0"
val jpackageInput = layout.buildDirectory.dir("jpackage-input")
val jpackageOutput = layout.buildDirectory.dir("jpackage")
val jpackageExecutable = javaToolchains.launcherFor {
    languageVersion = JavaLanguageVersion.of(21)
}.map { launcher ->
    launcher.executablePath.asFile.parentFile.resolve("jpackage.exe")
}

val prepareJpackageInput by tasks.registering(Sync::class) {
    dependsOn("fatJar")
    from(tasks.named<Jar>("fatJar").flatMap { it.archiveFile })
    into(jpackageInput)
    rename { "application.jar" }
}

val jpackageAppImage by tasks.registering(Exec::class) {
    group = "distribution"
    description = "Builds a portable Windows application with an embedded Java 21 runtime."
    dependsOn("test", prepareJpackageInput)
    inputs.dir(jpackageInput)
    inputs.files("config.json", "emails.txt", "content.txt")
    outputs.dir(jpackageOutput.map { it.dir(portableAppName) })

    doFirst {
        delete(jpackageOutput.map { it.dir(portableAppName) })
        jpackageOutput.get().asFile.mkdirs()
        commandLine(
            jpackageExecutable.get().absolutePath,
            "--type", "app-image",
            "--name", portableAppName,
            "--app-version", portableVersion,
            "--description", "Blue Archive automatic feedback tool",
            "--vendor", "auto-BlueArchive-feedback",
            "--input", jpackageInput.get().asFile.absolutePath,
            "--dest", jpackageOutput.get().asFile.absolutePath,
            "--main-jar", "application.jar",
            "--main-class", application.mainClass.get(),
            "--java-options", "-Dapp.home=\$APPDIR/..",
            "--win-console"
        )
    }

    doLast {
        copy {
            from("config.json", "emails.txt", "content.txt")
            into(jpackageOutput.get().dir(portableAppName))
        }
    }
}

tasks.register<Zip>("portableZip") {
    group = "distribution"
    description = "Builds a ZIP containing the portable Windows application and Java 21 runtime."
    dependsOn(jpackageAppImage)
    archiveFileName = "$portableAppName-$portableVersion-windows.zip"
    destinationDirectory = layout.buildDirectory.dir("distributions")
    from(jpackageOutput.map { it.dir(portableAppName) }) {
        into(portableAppName)
    }
}
