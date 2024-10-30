* Reverted fast buffer upload to old approach
  * The previous method (Using a streaming buffer) didn't show much of an extra performance improvement, but was a lot more complex/error-prone and doesn't work on older GPUs.
  * If you notice a significant performance drop or graphical glitches when updating to this version, please open an issue on the GitHub repository.
* 1.21.2+ only: Fixed performance regression on Apple GPUs
* 1.21.2+ only: Screen Batching: Only batch container items and chat screen
* 1.21.2+ only: Added workaround for item cooldown render order issue
