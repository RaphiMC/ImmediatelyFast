* Added new optimization: Avoid redundant framebuffer switching
  * This optimization reduces the number of framebuffer switches when rendering many different things in the world (entities, particles, block entities, ...) or the HUD to increase FPS.
* Fixed force draw handling (Fixes compatibility with Tweakermore and MiniHUD)
* Removed experimental universal HUD batching optimization
  * This optimization was disabled by default, but caused too many graphical issues when enabled, which are difficult to fix for a relatively small performance increase
