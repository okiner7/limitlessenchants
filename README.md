<p align="center">
  <img src="icon.png" alt="LimitlessEnchants Icon" width="128" height="128">
</p>

# LimitlessEnchants

**🔗 Links:** [Modrinth](https://modrinth.com/plugin/limitlessenchants) | [SpigotMC](https://www.spigotmc.org/resources/limitlessenchants-bypass-anvil-limits-gui-1-21.136866/) | [CurseForge](https://www.curseforge.com/minecraft/bukkit-plugins/limitlessenchants) | [bStats](https://bstats.org/plugin/bukkit/LimitlessEnchants/31895)

A lightweight, high-performance Paper/Purpur plugin designed to bypass vanilla Minecraft enchantment level limits and anvil repair restrictions. Enhance your server's gear progression by allowing players to combine and level up enchantments beyond their vanilla limits!

---

## 🚀 Features

* **In-Game GUI Config Editor:** Adjust maximum repair costs, enchantment limits, and toggle settings directly in-game using an interactive chest menu! No need to manually edit YAML files anymore.
* **No Enchantment Caps**: Configure custom maximum levels for every single enchantment individually (e.g., Sharpness X, Efficiency X, Fortune V).
* **Multi-Language Support**: Fully localized in English and Russian! Switch languages on the fly via the GUI or with a command.
* **Remove "Too Expensive!"**: Completely removes the annoying client-side anvil repair limit (replaces the 40+ level cap with a customizable maximum cost, defaulting to 39 levels).
* **Smart Anvil Merging**: Restores correct combining math for high-level enchantments. Combining two items/books of level `X` yields level `X + 1` (up to your configured cap).
* **Unsafe Combinations**: Allow players to combine mutually exclusive enchantments (like Mending + Infinity, or Sharpness + Smite).
* **Visual & Sound Effects**: Satisfying level-up sounds and enchantment particles when players successfully craft overpowered gear.
* **Highly Compatible**: Works seamlessly on Paper, Purpur, and other forks (tested on version 1.21.1+).
* **bStats Integration**: Includes built-in anonymous metrics to help track usage statistics.

---

## 💻 Commands

| Command | Description |
|:---|:---|
| `/limitlessenchants` (or `/le`) | Opens the interactive GUI Configuration Menu. |
| `/le reload` | Reloads the configuration from disk. |
| `/le lang <en/ru>` | Instantly switch the plugin's language. |

---

## 🔑 Permissions

You can granularly control who has access to the plugin's features:

| Permission | Description | Default |
|:---|:---|:---|
| `limitlessenchants.use` | Access to the `/le` GUI menu. | `op` |
| `limitlessenchants.reload` | Ability to use `/le reload` and `/le lang`. | `op` |
| `limitlessenchants.bypass.level` | Bypass vanilla enchantment max levels. | `true` |
| `limitlessenchants.bypass.cost` | Bypass the "Too Expensive!" anvil repair limit. | `true` |
| `limitlessenchants.bypass.conflicts` | Combine unsafe/conflicting enchantments (if enabled). | `true` |

---

## 📦 Installation

1. Download the plugin `.jar` file.
2. Place it in your server's `plugins/` directory.
3. Start or restart the server.
4. Type `/le` in-game to configure your limits!
