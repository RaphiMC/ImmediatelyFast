# ImmediatelyFast

ImmediatelyFast is an open source Fabric mod which improves the immediate mode rendering performance of the client.

It is designed to be lightweight and compatible with other mods. This makes it an ideal choice for modpacks or as a
replacement for other more aggressive optimization mods such as Exordium or Enhanced Block Entities.

## Installation

The mod is requires the Fabric loader and is compatible with Minecraft 1.19 - 1.19.3.

You can download the mod from [Modrinth](https://modrinth.com/mod/immediatelyfast)
or [Curseforge](https://www.curseforge.com/minecraft/mc-mods/immediatelyfast)
or [Github](https://github.com/RaphiMC/ImmediatelyFast/releases/latest).

## Optimizations

ImmediatelyFast generally optimizes all immediate mode rendering by using a custom buffer implementation which batches
draw calls.  
The following parts of the immediate mode rendering code are optimized:

- Entities
- Block entities (Especially signs)
- Particles
- Text rendering (Especially with high-res font texture packs)
- GUI/HUD
- Immediate mode rendering of other mods (ImmersivePortals benefits a lot from this)

It also features targeted optimizations where vanilla rendering code is being changed in order to run faster.  
The following parts of the rendering code are optimized with a more efficient implementation:

- Map rendering
- HUD rendering

## Performance
Here are some performance comparisons of areas the mod optimizes particularly well (Using IF 1.1.1 on Minecraft 1.19.3):

Test Hardware: Ryzen 5 1600, 32GB DDR4, GTX 1060

FPS Numbers were taken from external tools (in this case MSI Afterburner) and averaged over a couple of seconds.  
If you decide to test this yourself keep in mind that ImmediatelyFast can only improve FPS in a scenario where your CPU
is the bottleneck (Most likely the case if your GPU isn't ancient or you use very heavy shaders).
Slower CPUs will benefit more from this mod than really fast CPUs.

### Entity Rendering
Generally FPS should be 2x higher on busy servers.

#### Iris Disclaimer
Iris is supported but defeats some of ImmediatelyFast's optimizations, as Iris replaces the Entity rendering engine with
its own one.
Installing both together is supported and won't cause issues and ImmediatelyFast will still optimize other
parts of the rendering code which aren't optimized by Iris. If you only use Iris for the Entity rendering
optimizations, you can replace it with ImmediatelyFast instead.

Tested on a spigot server with 1000 cows in a 3x3 box on screen.

| Other mods      | Without ImmediatelyFast | With ImmediatelyFast | Improvement |
|-----------------|-------------------------|----------------------|-------------|
| None            | 18 FPS                  | 46 FPS               | 2.56x       |
| Sodium          | 22 FPS                  | 58 FPS               | 2.64x       |
| Iris and Sodium | 60 FPS                  | 58 FPS               | 0.97x       |

### Map Rendering
Generally FPS should be 5x higher when there are many maps on screen.

Tested on a fabric server with the [Image2Map](https://modrinth.com/mod/image2map) mod and around 930 maps on screen.

| Other mods      | Without ImmediatelyFast | With ImmediatelyFast | Improvement |
|-----------------|-------------------------|----------------------|-------------|
| None            | 50 FPS                  | 310 FPS              | 6.20x       |
| Sodium          | 47 FPS                  | 320 FPS              | 6.81x       |
| Iris and Sodium | 53 FPS                  | 290 FPS              | 5.47x       |

### HUD Rendering
Generally FPS should be around 30% higher in almost all scenarios.

#### Exordium Disclaimer
Exordium is supported but defeats some of ImmediatelyFast's optimizations, as Exordium replaces the HUD rendering with
a buffered non-immediate implementation.
Installing both together is supported and won't cause issues, it will even improve performance further.

Tested on a spigot server with different HUD elements on screen (Scoreboard, Potion effect overlay, Bossbars, Filled Chat, Extra hearts, Full Hotbar).

| Other mods          | Without ImmediatelyFast | With ImmediatelyFast | Improvement |
|---------------------|-------------------------|----------------------|-------------|
| None                | 225 FPS                 | 330 FPS              | 1.47x       |
| Sodium              | 270 FPS                 | 490 FPS              | 1.81x       |
| Iris and Sodium     | 270 FPS                 | 460 FPS              | 1.70x       |
| Exordium and Sodium | 750 FPS                 | 830 FPS              | 1.11x       |

## Compatibility

ImmediatelyFast is structured to interfere with mods as little as possible.
It should work fine with most if not all mods and modpacks.

Known incompatibilities:
- Optifabric

If you encounter any issues, please report them on
the [issue tracker](https://github.com/RaphiMC/ImmediatelyFast/issues) or feel free to join
my [Discord](https://discord.gg/dCzT9XHEWu).
