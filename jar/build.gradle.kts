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
    ).forEach {
        implementation(project(":miyukistructurepattern-$it"))
    }
}

