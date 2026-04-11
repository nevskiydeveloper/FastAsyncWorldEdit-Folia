tasks.register("build") {
    subprojects.forEach { subproject ->
        dependsOn(subproject.tasks.matching { it.name == "build" || it.name == "assemble" || it.name == "jar" })
    }
}
