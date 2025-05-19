* Fixed font atlas resizing state inconsistency (Fixes rare JVM crash when using certain resource packs)
* Improved compatibility with mods which don't render into the batching buffer while batching
* Speed up buffer upload on Apple GPUs
  * This "reverts" an internal 1.21.5 change which caused a performance regression on Apple GPUs
