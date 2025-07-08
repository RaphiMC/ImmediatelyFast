* Fixed font atlas resizing state inconsistency (Fixes rare JVM crash)
* Draw current batch when mods modify the projection matrix (Fixes rendering issue with VoxelMap)
* Improved compatibility with mods which don't render into the batching buffer while batching
* Ensure render layers are ordered even when transparent (Fixes rendering issue with Shoulder Surfing Reloaded)
* Removed experimental universal HUD batching optimization
  * This optimization was disabled by default, but caused too many graphical issues when enabled, which are difficult to fix for a relatively small performance increase
