import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    `maven-publish`
    kotlin("jvm") version "2.0.20"
    kotlin("kapt") version "2.0.20"
    id("io.github.goooler.shadow") version "8.1.8"
    id("org.ajoberstar.grgit") version "4.1.1"
}

var currentBranch: String = grgit.branch.current().name

allprojects {
    group = "gg.scala.friends"
    version = "2.1.2${
        if (currentBranch == "develop") "-dev" else ""
    }"

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.codemc.io/repository/maven-snapshots/")
        maven("https://jitpack.io")
        configureScalaRepository()
    }
}

dependencies {
    compileOnly(kotlin("stdlib"))
    compileOnly(fileTree("lib"))

    kapt("gg.scala.commons:bukkit:4.0.7")
    kapt("gg.scala.commons:core:4.0.7")
    kapt("me.lucko:helper:5.6.14")
    kapt("gg.scala.flavor:flavor:0.1.6")

    compileOnly("gg.scala.commons:bukkit:4.0.7")
    compileOnly("gg.scala.commons:core:4.0.7")
    compileOnly("gg.scala.commons:commons:4.0.7")
    compileOnly("gg.scala.commons:serversync:4.0.7")
    compileOnly("gg.scala.commons:serializers:4.0.7")
    compileOnly("gg.scala.commons:store:4.0.7")
    compileOnly("me.lucko:helper:5.6.14")
    compileOnly("gg.scala.flavor:flavor:0.1.6")

    compileOnly("gg.scala.store:spigot:1.0.0")
    compileOnly("gg.scala.store:shared:1.0.0")
    compileOnly("gg.scala.basics.plugin:scala-basics-plugin:1.0.6")

    compileOnly("gg.scala.aware:aware:2.0.0")
    compileOnly("com.github.cryptomorin:XSeries:13.2.0")
    compileOnly("io.lettuce:lettuce-core:6.2.0.RELEASE")
    compileOnly("org.mongodb:mongodb-driver-sync:4.9.0")

    implementation("net.wesjd:anvilgui:1.5.3-SNAPSHOT")

    compileOnly("gg.scala.spigot:server:1.1.3")
    compileOnly("gg.scala.lemon:bukkit:2.0.1")
    compileOnly("gg.scala.cloudsync:spigot:1.0.3")
}

kotlin {
    jvmToolchain(jdkVersion = 21)
}

kapt {
    correctErrorTypes = true
}

tasks.withType<ShadowJar> {
    archiveClassifier.set("")
    exclude(
        "**/*.kotlin_metadata",
        "**/*.kotlin_builtins",
        "META-INF/"
    )

    archiveFileName.set(
        "friends.jar"
    )
}

tasks.withType<JavaCompile> {
    options.compilerArgs.add("-parameters")
    options.fork()
    options.encoding = "UTF-8"
}

tasks.withType<KotlinCompile> {
    kotlinOptions.javaParameters = true
    kotlinOptions.jvmTarget = "21"
}

publishing {
    repositories {
        mavenLocal()
    }

    publications {
        register(
            name = "mavenJava",
            type = MavenPublication::class,
            configurationAction = shadow::component
        )
    }
}

tasks.getByName("build")
    .dependsOn("shadowJar")

fun RepositoryHandler.configureScalaRepository(dev: Boolean = false)
{
    maven("${property("artifactory_contextUrl")}/gradle-${if (dev) "dev" else "release"}") {
        name = "scala"
        credentials {
            username = property("artifactory_user").toString()
            password = property("artifactory_password").toString()
        }
    }
}
