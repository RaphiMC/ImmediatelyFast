* Fixed experimental screen batching on NeoForge and (Lex)Forge
* Reset shader color when force drawing the current batch (Fixes rendering issue with Detail Armor Bar mod)
* Render entity vertex buffer at the end of screen rendering (Fixes issues with mods which use the Iceberg tooltip rendering API)
* Added mechanism to allow resource packs with modified core shaders to declare compatible features
* Changed the versioning of ImmediatelyFast. The minor version is now incremented with every Minecraft update. The patch version is incremented with every ImmediatelyFast release. This aims to group all releases for a specific Minecraft version together. It also allows for more frequent releases because I can do a release for a single Minecraft version instead having to accumulate enough changes to justify a release for all supported versions.
