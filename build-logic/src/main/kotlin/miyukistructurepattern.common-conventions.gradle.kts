plugins {
    `java-library`
    id("com.github.johnrengelman.shadow")
}

repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
    maven("https://repo.spongepowered.org/maven/")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://oss.sonatype.org/content/repositories/snapshots/")
    maven("https://repo.aikar.co/content/groups/aikar/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.org/repository/maven-public/")
}

val libs = extensions.getByType(org.gradle.accessors.dm.LibrariesForLibs::class)

dependencies {
    implementation(libs.kyori.minimessage)
    implementation(libs.kyori.serializer.legacy)
    implementation(libs.kyori.serializer.gson)
    implementation(libs.kyori.serializer.plain)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)
    implementation(libs.jetbrains.annotations)

    implementation(libs.configurate.yaml)
}

tasks.compileJava {
    options.release.set(8)
    options.encoding = Charsets.UTF_8.name()
}

java {
    disableAutoTargetJvm()
}