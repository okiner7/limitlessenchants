# LimitlessEnchants ✨

**LimitlessEnchants** is a lightweight Paper/Bukkit plugin for Minecraft that completely removes the "Too Expensive!" limit from anvils and allows players to combine and upgrade enchantments beyond the standard vanilla caps.

## Features
- **No More "Too Expensive!"**: Repairs and combinations will never be blocked by the vanilla 39-level cap. Costs are clamped to a configurable maximum, allowing you to repair items infinitely.
- **Uncapped Enchantments**: Combine books and items in the anvil to achieve enchantment levels beyond vanilla limits (e.g., Sharpness VI, Protection V, Unbreaking IV).
- **Customizable Limits**: Set global default max levels, or specify precise max levels for each enchantment in `config.yml`.
- **bStats Integration**: Lightweight, anonymous statistics gathering.

## Configuration
When you run the plugin for the first time, a `config.yml` will be generated in the `plugins/LimitlessEnchants` folder. You can configure:
- `max-repair-cost`: The absolute maximum XP cost for any anvil operation (bypassing the "Too Expensive!" wall).
- `default-max-level`: Global max level for uncapped enchantments.
- `enchantments`: A section to set custom max limits per enchantment.

## Commands & Permissions
- `/limitlessenchants reload` (alias `/le reload`): Reloads the configuration file without restarting the server.
  - **Permission**: `limitlessenchants.reload` (Default: op)
- **Use Anvil Features**: 
  - **Permission**: `limitlessenchants.use` (Default: op)

## Installation
1. Compile the project using Maven (`mvn clean package`) or download the release `.jar`.
2. Drop the `.jar` into your server's `plugins` folder.
3. Restart the server.

## Supported Versions
Built against **Paper 1.21.1**.
