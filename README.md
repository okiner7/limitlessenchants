<p align="center">
  <img src="icon.png" alt="LimitlessEnchants Icon" width="128" height="128">
</p>

# LimitlessEnchants

A lightweight, high-performance Paper/Purpur plugin designed to bypass vanilla Minecraft enchantment level limits and anvil repair restrictions. Enhance your server's gear progression by allowing players to combine and level up enchantments beyond their vanilla limits!

---

## 🚀 Features

* **No Enchantment Caps**: Configure custom maximum levels for every single enchantment individually (e.g., Sharpness X, Efficiency X, Fortune V).
* **Remove "Too Expensive!"**: Completely removes the annoying client-side anvil repair limit (replaces the 40+ level cap with a customizable maximum cost, defaulting to 39 levels).
* **Smart Anvil Merging**: Restores correct combining math for high-level enchantments. Combining two items/books of level `X` yields level `X + 1` (up to your configured cap).
* **Dynamic Configuration**: Reload changes on the fly using `/le reload` without needing to restart your server.
* **Highly Compatible**: Works seamlessly on Paper, Purpur, and other forks (tested on version 1.21.1+).
* **bStats Integration**: Includes built-in anonymous metrics to help track usage statistics.

---

## 🛠️ Configuration (`config.yml`)

The default configuration file is clean, well-documented, and easy to customize:

```yaml
# The maximum repair cost allowed in the anvil (default is 39).
# Setting this to 39 or below completely removes the "Too Expensive!" message
# on the client and allows players to retrieve their items.
max-repair-cost: 39

# The default maximum level for unlisted enchantments (only used if use-default-max-for-unlisted is set to true).
default-max-level: 10

# Whether to use default-max-level for enchantments that are not explicitly listed below.
# If false (recommended), unlisted enchantments will use their standard vanilla limits.
use-default-max-for-unlisted: false

# Custom maximum levels for specific enchantments.
# Specify the enchantment name and its maximum level limit.
# You can use namespaced keys (e.g., minecraft:sharpness) or just the key name (e.g., sharpness).
enchantments:
  sharpness: 10
  protection: 10
  efficiency: 10
  unbreaking: 10
  fortune: 5
  looting: 5
  power: 10
  feather_falling: 10
```

---

## 💻 Commands & Permissions

| Command | Description | Permission | Default |
|:---|:---|:---|:---|
| `/limitlessenchants` (or `/le`) | Shows plugin help / info page. | `limitlessenchants.use` | `op` |
| `/le reload` | Reloads the configuration on the fly. | `limitlessenchants.reload` | `op` |

---

## 📦 Installation

1. Download the plugin `.jar` file.
2. Place it in your server's `plugins/` directory.
3. Start or restart the server.
4. Modify `plugins/LimitlessEnchants/config.yml` to set your desired enchantment caps.
5. Run `/le reload` to apply!
