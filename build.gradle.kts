import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.25"
    application
}

group = "org.jetbrains.kotlin"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("org.antlr:antlr4-runtime:4.7.1")
    implementation("org.antlr:ST4:4.0.8")
    implementation("org.antlr:antlr4:4.7.1")
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("javax.xml.bind:jaxb-api:2.3.1")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "21"
}

application {
    mainClass.set("org.jetbrains.kotlin.grammargenerator.MainKt")
}