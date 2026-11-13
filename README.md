# AKHI CONTROL PANEL 1.0.0

Two parts are included:

1. `plugin/` — Paper 1.21.4 server plugin. Admin-only `/menu`, performance and chunk controls, online player actions, and an admin console prompt.
2. `fabric-mod/` — Fabric 1.21.4 client mod. Press **G** to send `/menu` to the server. The server permission check remains authoritative.

## Safety notes

- Permission: `akhicontrol.admin`, default `op`.
- The Console item runs the next chat line as the server console. Keep this permission restricted to trusted admins.
- Performance presets change live per-world view and simulation distances. They are not host-wide VPS settings.
- The status item reports JVM heap usage, not total VPS RAM.
- No whitelist, Geyser/Floodgate configuration, ports, other services, or VPS-wide settings are changed.

## Build Paper plugin

Requires JDK 21+ and internet access:

```bash
cd plugin
gradle build
```

Output: `plugin/build/libs/AKHI-Control-Panel-1.0.0.jar`

Copy it to the Minecraft server `plugins/` folder and restart that Minecraft server.

## Build Fabric keybind mod

Requires JDK 21+ and internet access:

```bash
cd fabric-mod
gradle build
```

Output: `fabric-mod/build/libs/akhi-control-keybind-1.0.0.jar`

Install on each Java player's Fabric 1.21.4 client with Fabric Loader and Fabric API. Press **G** in-game. Change the key in Options → Controls → Key Binds if needed.

## Permission

The permission defaults to OP. With a permissions plugin:

```text
/lp user <player> permission set akhicontrol.admin true
```

Or grant operator from server console:

```text
op <player>
```

## Menu pages

- Control Center
- Performance: TPS and JVM heap, three presets
- Chunks & View Distance: view distance 6/8/10, simulation distance 4/5/6
- Admin Console: one command entered in chat, then returns to menu
- Players: left-click to kick, right-click to teleport

This is an initial functional version using Paper inventory menus, not a fully custom client-side graphical dashboard.