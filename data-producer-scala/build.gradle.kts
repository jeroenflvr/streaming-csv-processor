plugins {
    scala
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.13.14")
    implementation("org.apache.kafka:kafka-clients:3.7.0")
    testImplementation("org.scalatest:scalatest_2.13:3.2.18")
    testImplementation("org.mockito:mockito-core:5.2.0")
    testImplementation("org.junit.platform:junit-platform-launcher:1.10.2")
    testImplementation("org.scalatestplus:junit-4-13_2.13:3.2.18.0")
}

application {
    mainClass.set("dataproducer.Main")
}

tasks.test {
    testLogging {
        events("passed", "skipped", "failed")
    }
}
