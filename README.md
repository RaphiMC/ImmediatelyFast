# ImmediatelyFast

ImmediatelyFast is an open source Fabric mod which improves the immediate mode rendering performance of the client.

## Installation

The mod is requires the Fabric loader and is compatible with Minecraft 1.19 - 1.19.3.

You can download the mod from [Modrinth](https://modrinth.com/mod/immediatelyfast)
or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast)
or [Github Releases](https://github.com/RaphiMC/ImmediatelyFast/releases/latest).

## Optimizations

ImmediatelyFast generally optimizes all immediate mode rendering by using a custom buffer implementation which batches
draw calls.  
The following parts of the immediate mode rendering code are optimized:

- Entities
- Block entities
- Particles
- Text rendering
- GUIs
- Immediate mode rendering of other mods (ImmersivePortals benefits a lot from this)

It also features targeted optimizations where vanilla rendering code is being replaced with a more efficient implementation.  
The following parts of the rendering code are replaced with a more efficient implementation:

- Map rendering

## Performance
Here are some performance comparisons of areas the mod optimizes particularly well:


Test Hardware: Ryzen 5 1600, 32GB DDR4, GTX 1060  
FPS Numbers were taken from the F3 screen and averaged over a couple of seconds.
### Entity Rendering
Generally FPS should be 2x higher on busy servers and might also be up to 4x higher in cases where there are many entities.

The following table shows the performance improvements of the mod.  
Tested on a spigot server with 1000 cows in a 3x3 box and all on screen.  
Test Hardware: Ryzen 5 1600, 32GB DDR4, GTX 1060

| Other mods      | Without ImmediatelyFast | With ImmediatelyFast | Improvement |
|-----------------|-------------------------|----------------------|-------------|
| None            | 16 FPS                  | 40 FPS               | 2.5x        |
| Sodium          | 20 FPS                  | 58 FPS               | 2.9x        |
| Iris and Sodium | 48 FPS                  | 56 FPS               | 1.1x        |

### Map Rendering
Generally FPS should be 5x higher when there are many maps on screen.

The following table shows the performance improvements of the mod.  
Tested on a fabric server with the [Image2Map](https://modrinth.com/mod/image2map) mod and around 930 maps on screen.  
Test Hardware: Ryzen 5 1600, 32GB DDR4, GTX 1060

| Other mods      | Without ImmediatelyFast | With ImmediatelyFast | Improvement |
|-----------------|-------------------------|----------------------|-------------|
| None            | 43 FPS                  | 215 FPS              | 5x          |
| Sodium          | 44 FPS                  | 237 FPS              | 5.4x        |
| Iris and Sodium | 52 FPS                  | 230 FPS              | 4.4x        |

## Compatibility

The mod should work fine with almost all mods.  
Known incompatibilities:

- Optifabric

Iris is supported but defeats some of ImmediatelyFast's optimizations, as Iris replaces the Entity rendering engine with
its own one.
Installing both together is supported and won't cause issues. ImmediatelyFast will still optimize other
parts of the rendering code which aren't optimized by Iris. If you only use Iris for the Entity rendering
optimizations, you can replace it with ImmediatelyFast instead.

If you encounter any issues, please report them on
the [issue tracker](https://github.com/RaphiMC/ImmediatelyFast/issues) or feel free to join
my [Discord](https://discord.gg/dCzT9XHEWu).
