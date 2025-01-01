rootProject.name = "MiyukiStructurePattern"

pluginManagement {
        includeBuild("build-logic")

        repositories {
                mavenCentral()
                gradlePluginPortal()
        }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

sequenceOf(
        "spigot",
        "jar"
).forEach {
        include("miyukistructurepattern-$it")
        project(":miyukistructurepattern-$it").projectDir = file(it)
}

sequenceOf(
        "plotsquared-legacy",
        "plotsquared",
        "griefprevention",
        "griefdefender",
        "worldedit-6",
        "worldedit-7",
        "worldguard-6",
        "worldguard-7",
).forEach {
        include("miyukistructurepattern-$it")
        project(":miyukistructurepattern-$it").projectDir = file("compatibility/$it")
}