## Entity Visualizer

This mod is still under development! You can use it but there may be some bugs or breaking changes in the future.

[![License](https://img.shields.io/github/license/Fallen-Breath/fabric-mod-template.svg)](http://www.gnu.org/licenses/lgpl-3.0.html)

This mod uses Fallen's fabric mod template.

You need to have the server mod on the server to have accurate entity ticks. Doesn't work on singleplayer yet.

![entityvisualizer](https://github.com/lntricate1/EntityVisualizer/assets/29168747/9c2a6925-e3ee-41f2-bacf-453f9e039a7a)

## Features
### Utilities
- `getEntityData`: A hotkey to get entity position and motion, with identical entities grouped and counted. Supports fuse for TNT entities.
### Renderers
- `explosions`: Shows boxes at explosions.
- `explosionEntityRays`: Shows raycasts used in entity exposure calculation. *Note: entity may be desynced, i still need to ensure it's synced*
- `explosionBlockRays`: Shows minimum and maximum "rays" checked for block breaking. Renders red points that always get hit, yellow points that *can* be hit, and lines representing each ray.
- `explosionAffectedBlocks`: Shows minimum and maximum blocks destroyed by an explosion. Renders red boxes for blocks that always get blown up, and yellow boxes for blocks that *can* be blown up.
- `entityCreation`: Shows boxes at entity spawn locations.
- `entityTicks`: Shows boxes at entity tick locations.
- `entityVelocity`: Shows lines for entity velocity (one line for each tick).
- `entityTrajectory`: Shows lines for entity movement. Shows the direction of collision checks for entities that do them. Uses different color for normal movement and piston movement.
- `entityDeaths`: Shows boxes at entity death locations.
