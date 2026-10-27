plugins {
    id("java")
}

group = "com.lumi2021"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // Dependencias preexistentes - preservadas
    implementation("io.javalin:javalin:6.7.0")
    runtimeOnly("org.slf4j:slf4j-simple:2.0.16")

    // Necessaria apenas para desserializar o JSON recebido pela interface HTTP
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.3")
}

tasks.test {
    useJUnitPlatform()
}
tasks.register<JavaExec>("run") {
    group = "application"
    description = "Executa o programa principal"

    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.lumi2021.urna.Program")
}

// Teste de leitura sem abrir a interface Swing
tasks.register<JavaExec>("demoFirebase") {
    group = "application"
    description = "Consulta um candidato da API Firebase (ex.: Bulbasaur 12001)"
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("com.lumi2021.urna.integration.FirebaseIntegrationDemo")
}

tasks.register<JavaExec>("demoVotacao") {

    group = "application"

    description = "Testa o cliente HTTP de votacao"

    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set(
        "com.lumi2021.urna.integration.FirebaseVotingDemo"
    )
}

tasks.register<JavaExec>("demoVotacaoPost") {
    group = "verification"

    description = "Testa o registro de votos via HTTP POST"

    classpath = sourceSets["main"].runtimeClasspath

    mainClass.set(
        "com.lumi2021.urna.integration.FirebaseVotingPostDemo"
    )
}

