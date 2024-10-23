* Added support for 1.21.2/1.21.3
* Dropped support for 1.20.5/1.20.6
* Fixed wolf collar rendering order
* 1.21.2+ only: Rewrote fast buffer upload to have less overhead in high (1000+) FPS scenarios
* 1.21.2+ only: Deprecated the ImmediatelyFast batching API. Mojang added basic batching into the DrawContext class. ImmediatelyFast now uses and extends this system, so the custom API is no longer needed. To migrate your mod, simply remove all calls to the ImmediatelyFast batching API and make sure to use the DrawContext for your HUD rendering.
* 1.21.2+ only: Made experimental universal HUD batching more efficient
