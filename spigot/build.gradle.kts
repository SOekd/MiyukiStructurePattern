plugins {
    id("miyukistructurepattern.platform-conventions")
}

dependencies {
    compileOnly(libs.paper)

    implementation(libs.kyori.platform.bukkit)

    implementation(libs.xseries)

    implementation(libs.itemnbtapi)
    implementation(libs.bstats)

    implementation(libs.packetevents)

    implementation(libs.fastparticles)
}