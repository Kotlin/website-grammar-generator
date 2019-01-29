import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.3.11"
    application
}

group = "org.jetbrains.kotlin"
version = "0.1"

repositories {
    maven { setUrl("http://dl.bintray.com/jonnyzzz/maven") }
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.antlr:antlr4-runtime:4.7.1")
    compile("org.antlr:ST4:4.0.8")
    compile("org.antlr:antlr4:4.7.1")
    compile("com.xenomachina:kotlin-argparser:2.0.4")
    compile("org.jonnyzzz.kotlin.xml.bind:jdom:0.2.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions {
        freeCompilerArgs = listOf("-Xnew-inference")
    }
}

application {
    mainClassName = "org.jetbrains.kotlin.grammargenerator.MainKt"
}
