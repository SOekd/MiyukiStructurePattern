plugins {
    id("miyukistructurepattern.platform-conventions")
}

repositories {
    ivy {
        url = uri("https://ci.athion.net/job/PlotSquared-v3/lastSuccessfulBuild/artifact/target/")

        patternLayout {
            artifact("/[module]-[revision].jar")
        }

        metadataSources { artifact() }
    }
}

dependencies {
    compileOnly(libs.paper)
    implementation(project(":miyukistructurepattern-spigot"))
    compileOnly(libs.plotsquared.legacy)
}