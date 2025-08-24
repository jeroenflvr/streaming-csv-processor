plugins {
    kotlin("jvm") version "1.9.22"
    application
}

kotlin {
    jvmToolchain(21)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.kafka:kafka-clients:3.7.0")
}

application {
    mainClass.set("dataproducer.Main")
}
