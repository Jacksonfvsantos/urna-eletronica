plugins {
    java
    application
}

group = "com.lumi2021"
version = "1.0-SNAPSHOT"

repositories { mavenCentral() }

dependencies {
    implementation("com.google.firebase:firebase-admin:9.11.0")
    implementation("io.javalin:javalin:6.7.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java { toolchain { languageVersion.set(JavaLanguageVersion.of(21)) } }

application { mainClass.set("com.lumi2021.urna.Program") }

tasks.test { useJUnitPlatform() }
