# WaypointTp (PaperMC 1.21+)
[日本語版README](README-ja.md)

A lightweight Paper plugin (Kotlin) that lets players **save locations**, **teleport via a GUI**, and **delete waypoints with a two‑step confirm**.  
Optionally, the plugin **autoplaces a “menu opener” item** in the **leftmost hotbar slot (slot 0)** for every online/joining player; right‑click it to open the menu.

> Built for Paper 1.21.x (Java 21). Uses Inventory GUI + PDC for reliable item/entry identity and `teleportAsync` for safe teleports into possibly-unloaded chunks. :contentReference[oaicite:0]{index=0}

---

## Features

- **Inventory GUI** for listing waypoints and teleporting with a single click.
- **Save current location** via command or GUI “Add” button.
- **Two‑step deletion in GUI** (Right‑click to arm → Right‑click again within 10s to confirm).
- **(Optional) Hotbar opener item**: automatically placed at slot `0` for all players; right‑click opens the GUI.
- **Localizable messages** via `messages.yml` using **MiniMessage** tags. :contentReference[oaicite:1]{index=1}

---

## Requirements

- **Paper 1.21.x** server  
- **Java 21** (required by modern Paper) :contentReference[oaicite:2]{index=2}

If you need to install Java, see Paper’s Java install guide. :contentReference[oaicite:3]{index=3}

---

## Quick start (Install the plugin)

1. **Download / build** the plugin JAR (see _Build from source_ below).  
2. Place the JAR into your server’s `plugins/` folder.  
3. Start the server; Paper will load the plugin automatically. See Paper’s getting‑started docs if you’re new to Paper. :contentReference[oaicite:4]{index=4}

---

## Commands

| Command | Description |
|---|---|
| `/wp ui` | Open the waypoint GUI. |
| `/wp set <name>` | Save your current location as `<name>`. |
| `/wp tp <name>` | Teleport to a saved waypoint by name. Uses `teleportAsync`. :contentReference[oaicite:5]{index=5} |
| `/wp tpp <player>` | Teleport to another player’s current location. |
| `/wp reload` | Reload `messages.yml` (and other config the plugin supports). |

> Deleting waypoints is done from the GUI (two‑step right‑click confirm), not via a command.

---

## Permissions

These permission nodes are defined in `plugin.yml`. The **`default: true`** examples below mean **everyone has them by default**, not only OPs. Valid defaults are `true`, `false`, `op`, `not op`. :contentReference[oaicite:6]{index=6}

| Node | Default | Purpose |
|---|---:|---|
| `waypoints.use` | `true` | Allow `/wp` usage (open GUI, etc.). |
| `waypoints.create` | `true` | Allow `/wp set` and GUI add. |
| `waypoints.delete` | `true` | Allow GUI deletion. |
| `waypoints.tpp` | `true` | Allow `/wp tpp`. |
| `waypoints.reload` | `op` | Allow `/wp reload`. |

> You can tailor defaults or manage per‑group with a permissions plugin later.

---

## Configuration

- `messages.yml` — all user‑facing messages. The plugin uses **MiniMessage** so you can style text with tags such as `<yellow>`, `<bold>`, and click actions. See MiniMessage docs for the full syntax. :contentReference[oaicite:7]{index=7}

Reload messages in‑game with `/wp reload`.

---

## How it works (tech notes)

- **GUI:** a custom `InventoryHolder` identifies our menus reliably (don’t rely on titles). See Paper’s project setup/structure when organizing sources. :contentReference[oaicite:8]{index=8}
- **Identity:** items/entries carry a UUID in **PDC (Persistent Data Container)**, stored on `ItemMeta`. :contentReference[oaicite:9]{index=9}
- **Teleports:** use **`Player#teleportAsync`** to avoid sync chunk loads if the target area is not loaded yet. :contentReference[oaicite:10]{index=10}
- **Distribution:** the opener item is tagged by PDC and distributed on `PlayerJoinEvent` and to all currently online players (`Bukkit.getOnlinePlayers()`). :contentReference[oaicite:11]{index=11}

---

## Build from source

This project uses **Gradle (Kotlin DSL)** and the **Shadow plugin** to produce a single distributable JAR (includes Kotlin stdlib, etc.).  
- Shadow plugin info & plugin ID: `com.gradleup.shadow`. :contentReference[oaicite:12]{index=12}

```bash
# On Windows
.\gradlew.bat clean build

# On macOS/Linux
./gradlew clean build
