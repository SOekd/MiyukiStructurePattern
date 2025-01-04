plugins {
    id("miyukistructurepattern.publishing-conventions")
}

dependencies {
    sequenceOf(
        "spigot"
    ).forEach {
        implementation(project(path = ":miyukistructurepattern-$it", configuration = "shadow"))
    }

    sequenceOf(
        "plotsquared-legacy",
        "plotsquared",
        "griefprevention",
        "griefdefender",
        "worldedit-6",
        "worldedit-7",
    ).forEach {
        implementation(project(":miyukistructurepattern-$it"))
    }
}

