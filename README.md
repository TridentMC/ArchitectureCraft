<img src="https://raw.githubusercontent.com/TridentMC/ArchitectureCraft/1.12/.github/logo.png" align="right" width="180px"/>

# Architecture Craft - TridentMC
[>> Downloads <<](https://github.com/TridentMC/ArchitectureCraft/releases)

*Distinguished architectural features for your Minecraft buildings.*

**This mod is open source and under a permissive license.** And as such,
can be included in any modpack on any platform without prior permission.
We appreciate hearing about people using our mods, but you do not need
to ask to use them. See the [LICENSE file](LICENSE) for more details.

ArchitectureCraft provides a wide variety of good-looking architectural features including sloped roofs, glazable window frames, arches and other rounded blocks, classical pillars and entablatures, and railings for balconies and stairways.
ArchitectureCraft blocks may be made from almost any vanilla material and many materials added by mods as well.


Build with Java 25 and Gradle 9.1.0:

```sh
gradle build --max-workers=1 --no-parallel
gradle runClient
```

For local Compound development, publish its snapshot first from the Compound checkout:

```sh
gradle :publishToMavenLocal -PversionTag=-SNAPSHOT --max-workers=1 --no-parallel
```

ArchitectureCraft resolves Compound from `mavenLocal()` before the Trident Maven repository.
Run `gradle test` for headless rendering, menu, and UI tests. The tests write
previews under `build/reports/ui`. Run
`gradle runGameTestServer` to check mod loading without opening a game window.

Use the sawbench's categories to choose a shape and insert a building material.
