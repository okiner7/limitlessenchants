package net.vinio.limitlessenchants;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ConfigGUI implements Listener {

    private final LimitlessEnchants plugin;

    public ConfigGUI(LimitlessEnchants plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        String title = plugin.getMessage("gui.main-title");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // 1. Unsafe Combinations
        boolean unsafe = plugin.isAllowUnsafeCombinations();
        inv.setItem(10, createItem(
                unsafe ? Material.LIME_DYE : Material.RED_DYE,
                plugin.getMessage("gui.unsafe-combines"),
                plugin.getMessage("gui.current") + (unsafe ? plugin.getMessage("gui.enabled") : plugin.getMessage("gui.disabled")),
                "",
                plugin.getMessage("gui.click-to-toggle")
        ));

        // 2. Effects
        boolean effects = plugin.isEffectsEnabled();
        inv.setItem(12, createItem(
                effects ? Material.LIME_DYE : Material.RED_DYE,
                plugin.getMessage("gui.effects"),
                plugin.getMessage("gui.current") + (effects ? plugin.getMessage("gui.enabled") : plugin.getMessage("gui.disabled")),
                "",
                plugin.getMessage("gui.click-to-toggle")
        ));

        // 3. Max Repair Cost
        inv.setItem(14, createItem(
                Material.ANVIL,
                plugin.getMessage("gui.max-repair-cost"),
                plugin.getMessage("gui.current") + "§f" + plugin.getMaxRepairCost(),
                "",
                plugin.getMessage("gui.left-click"),
                plugin.getMessage("gui.right-click"),
                plugin.getMessage("gui.shift-click", "10")
        ));

        // 4. Default Max Level
        inv.setItem(16, createItem(
                Material.ENCHANTING_TABLE,
                plugin.getMessage("gui.default-max-level"),
                plugin.getMessage("gui.current") + "§f" + plugin.getDefaultMaxLevel(),
                "",
                plugin.getMessage("gui.left-click"),
                plugin.getMessage("gui.right-click"),
                plugin.getMessage("gui.shift-click", "5")
        ));

        // 5. Enchantments Link
        inv.setItem(26, createItem(
                Material.ENCHANTED_BOOK,
                plugin.getMessage("gui.enchants-menu"),
                plugin.getMessage("gui.enchants-menu-lore1"),
                plugin.getMessage("gui.enchants-menu-lore2")
        ));

        // 6. Language Toggle
        inv.setItem(18, createItem(
                Material.PAPER,
                plugin.getMessage("gui.language"),
                plugin.getMessage("gui.language-lore"),
                "",
                plugin.getMessage("gui.click-to-toggle")
        ));

        player.openInventory(inv);
    }

    public void openEnchantsMenu(Player player) {
        String title = plugin.getMessage("gui.enchants-title");
        Inventory inv = Bukkit.createInventory(null, 54, title);

        int slot = 0;
        if (plugin.getConfig().isConfigurationSection("enchantments")) {
            for (String key : plugin.getConfig().getConfigurationSection("enchantments").getKeys(false)) {
                if (slot >= 53) break;
                int level = plugin.getConfig().getInt("enchantments." + key);
                
                inv.setItem(slot, createItem(
                        Material.ENCHANTED_BOOK,
                        "§b" + key,
                        plugin.getMessage("gui.max-level", String.valueOf(level)),
                        "",
                        plugin.getMessage("gui.left-click"),
                        plugin.getMessage("gui.right-click"),
                        plugin.getMessage("gui.shift-click", "5")
                ));
                slot++;
            }
        }

        inv.setItem(53, createItem(
                Material.BARRIER,
                plugin.getMessage("gui.back")
        ));

        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        String mainTitle = plugin.getMessage("gui.main-title");
        String enchantsTitle = plugin.getMessage("gui.enchants-title");

        if (title.equals(mainTitle)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int slot = event.getSlot();
            ClickType click = event.getClick();

            if (slot == 10) {
                boolean current = plugin.getConfig().getBoolean("allow-unsafe-combinations", false);
                plugin.getConfig().set("allow-unsafe-combinations", !current);
                saveAndRefresh(player);
                openMainMenu(player);
            } else if (slot == 12) {
                boolean current = plugin.getConfig().getBoolean("effects.enabled", true);
                plugin.getConfig().set("effects.enabled", !current);
                saveAndRefresh(player);
                openMainMenu(player);
            } else if (slot == 14) {
                int current = plugin.getConfig().getInt("max-repair-cost", 39);
                int change = click.isShiftClick() ? 10 : 1;
                if (click.isLeftClick()) current += change;
                else if (click.isRightClick()) current -= change;
                if (current < 1) current = 1;
                plugin.getConfig().set("max-repair-cost", current);
                saveAndRefresh(player);
                openMainMenu(player);
            } else if (slot == 16) {
                int current = plugin.getConfig().getInt("default-max-level", 10);
                int change = click.isShiftClick() ? 5 : 1;
                if (click.isLeftClick()) current += change;
                else if (click.isRightClick()) current -= change;
                if (current < 1) current = 1;
                plugin.getConfig().set("default-max-level", current);
                saveAndRefresh(player);
                openMainMenu(player);
            } else if (slot == 18) {
                String currentLang = plugin.getConfig().getString("language", "en");
                String newLang = currentLang.equalsIgnoreCase("en") ? "ru" : "en";
                plugin.getConfig().set("language", newLang);
                saveAndRefresh(player);
                openMainMenu(player);
            } else if (slot == 26) {
                openEnchantsMenu(player);
            }
        } else if (title.equals(enchantsTitle)) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

            int slot = event.getSlot();
            ClickType click = event.getClick();

            if (slot == 53) {
                openMainMenu(player);
                return;
            }

            if (clickedItem.getType() == Material.ENCHANTED_BOOK) {
                ItemMeta meta = clickedItem.getItemMeta();
                if (meta != null && meta.hasDisplayName()) {
                    String enchantName = org.bukkit.ChatColor.stripColor(meta.getDisplayName());
                    int current = plugin.getConfig().getInt("enchantments." + enchantName, 10);
                    int change = click.isShiftClick() ? 5 : 1;
                    if (click.isLeftClick()) current += change;
                    else if (click.isRightClick()) current -= change;
                    if (current < 1) current = 1;
                    plugin.getConfig().set("enchantments." + enchantName, current);
                    saveAndRefresh(player);
                    openEnchantsMenu(player);
                }
            }
        }
    }

    private void saveAndRefresh(Player player) {
        plugin.saveConfig();
        plugin.loadPluginConfig();
        try {
            player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
        } catch (IllegalArgumentException ignored) {}
    }

    private ItemStack createItem(Material material, String name, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            if (loreLines != null && loreLines.length > 0) {
                List<String> lore = new ArrayList<>();
                for (String line : loreLines) {
                    lore.add(line);
                }
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }
}
