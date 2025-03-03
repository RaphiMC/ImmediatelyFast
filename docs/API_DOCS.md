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

## Batching
ImmediatelyFast batches HUD rendering by default. If your mod only renders HUD elements, you don't need to do anything, because ImmediatelyFast will automatically batch your HUD elements.

To manually batch some of your rendering code, you can use the ``BatchingBuffers`` class. Simply wrap your code in ``BatchingBuffers.runBatched`` and it will be batched.

## Old API

### Main API class
The main API class is ``net.raphimc.immediatelyfastapi.ImmediatelyFastApi``. It contains access to the batching system and the currently loaded config.
