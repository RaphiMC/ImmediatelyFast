# Developer API documentation

### Including ImmediatelyFast using Gradle
To use ImmediatelyFast with Gradle you can use the Modrinth maven server:
```groovy
repositories {
    maven { url "https://api.modrinth.com/maven" }
}

dependencies {
    modImplementation("maven.modrinth:immediatelyfast:x.x.x") // Get latest version from modrinth
}
```

### Main API class
The main API class is ``net.raphimc.immediatelyfastapi.ImmediatelyFastApi``. It contains access to the batching system and the currently loaded config.

### Accessing internals
While it is not recommended to access internals of ImmediatelyFast you can do so by using classes in the ``net.raphimc.immediatelyfast`` package. These classes are not guaranteed to be stable and may change at any time, so make sure to build your code in a fail-safe manner if you decide to use them.
