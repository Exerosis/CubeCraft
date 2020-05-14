plugins { kotlin("jvm") version("1.3.72") }

group = "com.github.exerosis.cubecraft"
version = "1.0.0"

repositories { mavenCentral() }

dependencies {
    implementation(kotlin("stdlib-jdk8"))
    compileOnly(files("/Server/Spigot.jar"))
}

tasks.jar {
    archiveFileName.set("${project.name}.jar")
    destinationDirectory.set(file("/Server/plugins"))
    from(configurations["runtimeClasspath"].map(::zipTree))
}