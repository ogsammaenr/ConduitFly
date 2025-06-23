# ConduitFly

**ConduitFly** is a Minecraft plugin designed for BentoBox -based skyblock servers. It allows players to place customizable **Conduit blocks** on their island to grant **temporary flight** in a configurable radius. Flight duration, area, and fall-damage protection are all customizable based on the player's **rank**.

---

## ✨ Features

- 🔗 **BentoBox integration**: Flight is managed per-island
- 🌀 **Placeable Conduit blocks** to grant flight in range
- 💸 **Rank system**: Players can upgrade ranks with Vault economy
- 🎯 **Visual particle area indicator**
- 🔧 Fully configurable via **YAML**
- 💾 Supports both **SQLite** and **MySQL**
- 🔄 `/conduitfly reload` to hot-reload changes
- 🖱️ Paginated **GUI menus** for rank interaction

---

## 🔧 Dependencies

| Dependency   | Purpose                                   |
|--------------|-------------------------------------------|
| BentoBox     | Island data access (currently required due to storage logic; may become a soft-depend in future versions) |
| Vault        | Economy integration                       |
| Economy plugin | (EssentialsX, CMI, etc.)                |

> The plugin will not load if required dependencies are missing.

---

## 💻 Installation

1. Drop `ConduitFly.jar` into your `plugins/` directory.
2. Start the server. The following files will be created:
   - `config.yml`
3. Adjust settings to your needs.
4. Use `/conduitfly reload` to apply changes without restarting.

---

## 🏆 Rank System

Players can open a GUI with `/conduitfly rankup` to view and upgrade their rank. Each rank defines:

- ✈️ **Flight duration** (in seconds)
- 🌀 **Conduit radius**
- 🛡️ Optional **fall damage prevention**
- 💰 **Upgrade cost**

---

## 🛠️ Commands

| Command               | Description                                       |
|-----------------------|---------------------------------------------------|
| `/conduitfly reload`  | Reload the configuration                         |
| `/conduitfly rankup`  | Open the GUI to view/upgrade ranks               |
| `/conduitfly area`    | Visually display the conduit’s active range      |

---

## 🧩 Support & Contribution

Found a bug, have a feature request, or just want to contribute?

📬 [Open an issue or pull request on GitHub](https://github.com/ogsammaenr/ConduitFly/issues)

We welcome all feedback and contributions!

## 📁 Config Overview

- `conduit.material`: Which block grants flight (default: `CONDUIT`)
- `ranks`: Defines each rank’s properties
- `particles`: Visual effect shown within conduit area
- `storage`: Choose between `sqlite` or `mysql`
- `rank-gui`: Customize GUI layout and icons

See the `config.yml` for full documentation.

---

## ⚙️ Storage Options

| Type    | Description                                  |
|---------|----------------------------------------------|
| SQLite  | Default. Lightweight and easy to use         |
| MySQL   | For external or large-scale server networks  |

---

## 📜 License

This project is licensed under the **MIT License**.  
You are free to use, modify, and distribute the plugin as long as the original license and copyright notice are included.

---
