* Added NeoForge support (1.20.2 and above)
* Added 1.20.3 and 1.20.4 support
* Reduced memory allocations by avoiding the java Stream API
* Moved away from a single jar for all loaders since NeoForge uses the exact same metadata files as Forge
* Updated dependencies
* Don't include MixinExtras in the jar for loaders which have it built-in
