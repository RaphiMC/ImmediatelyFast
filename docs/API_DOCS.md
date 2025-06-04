# Developer API documentation

## Including ImmediatelyFast using Gradle
To use ImmediatelyFast with Gradle you can use the Modrinth maven server:
```groovy
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation("maven.modrinth:immediatelyfast:x.x.x") // Get latest version from modrinth
}
```

## Main Class
The main class is ``net.raphimc.immediatelyfast.ImmediatelyFast``. It holds the currently loaded config, runtime config and the version of the mod.
