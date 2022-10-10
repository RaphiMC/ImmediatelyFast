# ImmediatelyFast

ImmediatelyFast is an open source Fabric mod which improves the immediate mode rendering performance of the client.

## Installation

The mod is requires the Fabric loader and is compatible with Minecraft 1.19 and higher.

You can download the mod from [Modrinth](https://modrinth.com/mod/immediatelyfast)
or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast) or [Github Releases](https://github.com/RaphiMC/ImmediatelyFast/releases/latest).

## Optimizations

The following parts of the immediate mode rendering code are optimized:

- Entities
- Block entities
- Particles
- Text rendering (partially)
- GUIs (partially)

### Performance

Generally FPS should be 2x higher on busy servers and might also be up to 4x higher in cases where there are many
entities.

The following table shows the performance improvements of the mod.  
Tested on a server with 1000 entities in the visible view distance.  
Test Hardware: Ryzen 5 1600, 32GB DDR4, GTX 1060

| Other mods  | Without ImmediatelyFast | With ImmediatelyFast |
|-------------|-------------------------|----------------------|
| Vanilla     | 7 FPS                   | 21 FPS               |
| Sodium      | 10 FPS                  | 30 FPS               |

## Compatibility

The mod should work fine with almost all mods.  
Known incompatibilities:

- Optifabric

Iris is supported but defeats some of the mods optimizations, as it replaces the Entity rendering engine with its own one.

If you encounter any issues, please report them on
the [issue tracker](https://github.com/RaphiMC/ImmediatelyFast/issues) or feel free to join
my [Discord](https://discord.gg/dCzT9XHEWu).