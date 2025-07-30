# WaypointTp (PaperMC 1.21+)
[日本語版README](README-ja.md)

A lightweight Paper plugin (Kotlin) that lets players **save locations**, **teleport via a GUI**, and **delete waypoints with a two‑step confirm**.  
Optionally, the plugin **autoplaces a “menu opener” item** in the **leftmost hotbar slot (slot 0)** for every online/joining player; right‑click it to open the menu.

---

## Features

- **Inventory GUI** for listing waypoints and teleporting with a single click.
- **Save current location** via command or GUI “Add” button.
- **Two‑step deletion in GUI** (Right‑click to arm → Right‑click again within 10s to confirm).
- **(Optional) Hotbar opener item**: automatically placed at slot `0` for all players; right‑click opens the GUI.
- **Localizable messages** via `messages.yml` using **MiniMessage** tags.

---

## Requirements

- **Paper 1.21.x** server  
- **Java 21+**

---

## Quick start (Install)

1. **Download / build** the plugin JAR (see _Build from source_ below).  
2. Put the JAR into your server’s `plugins/` folder.  
3. Start the server (Paper will load the plugin automatically).

---

## Commands

| Command | Description |
|---|---|
| `/wp ui` | Open the waypoint GUI. |
| `/wp set <name> [x y z] [yaw pitch] [world]` | Save a waypoint. Missing arguments default to your **current** x/y/z, yaw/pitch and world. Examples: `/wp set home 100 64 -30`, `/wp set base 0 80 0 180 0 world_nether`. |
| `/wp tp <name>` | Teleport to a saved waypoint by name (uses `teleportAsync`). |
| `/wp tpp <player>` | Teleport to another player’s current location. |
| `/wp reload` | Reload `messages.yml` (and other future configs). |

> Deleting waypoints is done from the GUI (two‑step right‑click confirm).

---

## Permissions

Defined in `plugin.yml`. The defaults below make the plugin usable by everyone (except `/wp reload` which is for ops).

| Node | Default | Purpose |
|---|---:|---|
| `waypoints.use` | `true` | Allow `/wp` usage (open GUI, etc.). |
| `waypoints.create` | `true` | Allow `/wp set` and GUI add. |
| `waypoints.delete` | `true` | Allow GUI deletion. |
| `waypoints.tpp` | `true` | Allow `/wp tpp`. |
| `waypoints.reload` | `op` | Allow `/wp reload`. |

---

## Configuration

- `messages.yml` — all user‑facing messages (MiniMessage tags supported).  
  Reload with `/wp reload`.

---

## How it works (tech notes)

- **GUI:** a custom `InventoryHolder` identifies our menus reliably (avoid title checks).
- **Identity:** items/entries carry a UUID in **PDC (Persistent Data Container)**, stored on `ItemMeta`.
- **Teleports:** use **`Player#teleportAsync`** to avoid sync chunk loads when the target is not loaded.
- **Distribution:** the opener item is tagged by PDC and distributed on `PlayerJoinEvent` and to all currently online players.

---

## Build from source

This project uses **Gradle (Kotlin DSL)**. A shaded JAR can be produced with the Shadow plugin if needed.

```bash
# Windows
.\gradlew.bat clean build

# macOS/Linux
./gradlew clean build
