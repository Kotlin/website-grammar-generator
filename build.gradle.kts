import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
}

group = "org.jetbrains.kotlin"
version = "0.1"

repositories {
    maven { setUrl("http://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.antlr:antlr4-runtime:4.7.1")
    compile("org.antlr:ST4:4.0.8")
    compile("org.antlr:antlr4:4.7.1")
    compile("com.github.mifmif:generex:1.0.2")
    compile("com.xenomachina:kotlin-argparser:2.0.4")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions {
        freeCompilerArgs = listOf("-Xnew-inference")
    }
}