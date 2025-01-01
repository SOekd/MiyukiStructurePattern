plugins {
    id("miyukistructurepattern.common-conventions")
    id("com.github.johnrengelman.shadow")
}

tasks {
    shadowJar {
        archiveFileName.set("MiyukiStructurePattern-${project.version}.jar")

        mergeServiceFiles()

        val internalLibs = "app.miyuki.miyukistructurepattern.libs"

        listOf(
            "com.cryptomorin.xseries",
            "io.leangen.geantyref",
            "org.jetbrains",
            "org.intellij",
            "org.spongepowered.configurate",
            "de.tr7zw.changeme.nbtapi",
            "com.google.gson",
            "org.bstats",
            "net.kyori",
            "fr.mrmicky.fastparticles",
        ).forEach {
            relocate(it, "$internalLibs.$it")
        }


    }

    jar {
        manifest {
            attributes("author" to "SOekd")
        }
    }

    processResources {
        expand("version" to project.version)
    }
}