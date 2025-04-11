plugins {
    kotlin("jvm") version "2.1.20"
    id("io.ktor.plugin") version "3.1.2"
    kotlin("plugin.serialization") version "2.1.20"
    application
}

group = "com.github.stekl0"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-server-core-jvm")
    implementation("io.ktor:ktor-server-netty-jvm")
    implementation("io.ktor:ktor-client-core")
    implementation("io.ktor:ktor-client-okhttp")
    implementation("io.ktor:ktor-client-content-negotiation")
    implementation("io.ktor:ktor-serialization-kotlinx-json")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    implementation("org.slf4j:slf4j-simple:2.0.17")
    implementation("dnsjava:dnsjava:3.6.3")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(23)
}

val apiKeyFromGradleProps: Provider<String> = providers.gradleProperty("apiKeyGemini")

application {
    mainClass.set("MainKt")
}

tasks.named<JavaExec>("run") {
    environment("API_KEY", apiKeyFromGradleProps.get())
}
