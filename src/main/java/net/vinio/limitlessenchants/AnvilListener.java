package net.vinio.limitlessenchants;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.Repairable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class AnvilListener implements Listener {

    private final LimitlessEnchants plugin;

    public AnvilListener(LimitlessEnchants plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        AnvilInventory inventory = event.getInventory();
        ItemStack leftItem = inventory.getItem(0);
        ItemStack rightItem = inventory.getItem(1);
        Player player = (Player) event.getView().getPlayer();

        if (leftItem == null || leftItem.getType() == Material.AIR) {
            return;
        }

        ItemStack vanillaResult = event.getResult();
        ItemStack result = vanillaResult;

        // Fallback: If vanilla result is null but items are compatible, create result manually
        if (result == null || result.getType() == Material.AIR) {
            if (rightItem != null && rightItem.getType() != Material.AIR && areCompatible(leftItem, rightItem)) {
                result = leftItem.clone();
                // Repair durability if they are the same type
                if (leftItem.getType() == rightItem.getType()) {
                    if (leftItem.getItemMeta() instanceof org.bukkit.inventory.meta.Damageable) {
                        org.bukkit.inventory.meta.Damageable leftDamageable = (org.bukkit.inventory.meta.Damageable) leftItem.getItemMeta();
                        org.bukkit.inventory.meta.Damageable rightDamageable = (org.bukkit.inventory.meta.Damageable) rightItem.getItemMeta();
                        
                        int maxDurability = leftItem.getType().getMaxDurability();
                        int leftDamage = leftDamageable.getDamage();
                        int rightDamage = rightDamageable.getDamage();
                        
                        int durabilityRestored = (maxDurability - rightDamage) + (maxDurability * 12 / 100);
                        int newDamage = Math.max(0, leftDamage - durabilityRestored);
                        
                        ItemMeta resultMeta = result.getItemMeta();
                        if (resultMeta instanceof org.bukkit.inventory.meta.Damageable) {
                            ((org.bukkit.inventory.meta.Damageable) resultMeta).setDamage(newDamage);
                            result.setItemMeta(resultMeta);
                        }
                    }
                }
            } else {
                // Not compatible, leave it null/air
                return;
            }
        } else {
            result = result.clone();
        }

        // Apply new name if there's rename text
        String renameText = inventory.getRenameText();
        if (renameText != null && !renameText.trim().isEmpty()) {
            ItemMeta resultMeta = result.getItemMeta();
            if (resultMeta != null) {
                resultMeta.setDisplayName(renameText);
                result.setItemMeta(resultMeta);
            }
        }

        // Merge enchantments
        if (rightItem != null && rightItem.getType() != Material.AIR) {
            Map<Enchantment, Integer> mergedEnchants = mergeEnchantments(leftItem, rightItem, player);
            applyEnchantments(result, mergedEnchants);
        } else {
            // Keep left enchantments on rename/repair
            Map<Enchantment, Integer> leftEnchants = getEnchantments(leftItem);
            applyEnchantments(result, leftEnchants);
        }

        // Apply result to event
        event.setResult(result);

        // Determine repair cost
        int vanillaCost = inventory.getRepairCost();
        if (vanillaCost <= 0 && rightItem != null && rightItem.getType() != Material.AIR) {
            // Calculate a fallback cost if vanilla aborted early
            int leftPenalty = 0;
            if (leftItem.getItemMeta() instanceof Repairable) {
                leftPenalty = ((Repairable) leftItem.getItemMeta()).getRepairCost();
            }
            int rightPenalty = 0;
            if (rightItem.getItemMeta() instanceof Repairable) {
                rightPenalty = ((Repairable) rightItem.getItemMeta()).getRepairCost();
            }
            
            int enchantCost = 0;
            Map<Enchantment, Integer> rightEnchants = getEnchantments(rightItem);
            for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
                enchantCost += entry.getValue();
            }
            
            vanillaCost = leftPenalty + rightPenalty + enchantCost;
            if (vanillaCost <= 0) {
                vanillaCost = 2; // Default fallback
            }
        }

        // Clamp the cost if they have bypass permission
        int finalCost = vanillaCost;
        if (player.hasPermission("limitlessenchants.bypass.cost")) {
            int maxCost = plugin.getMaxRepairCost();
            finalCost = Math.min(vanillaCost, maxCost);
        }

        if (finalCost <= 0 && vanillaCost > 0) {
            finalCost = 1;
        }

        // Set repair cost immediately
        inventory.setRepairCost(finalCost);

        // Reinforce cost with 1-tick delay task to prevent override
        final int costToSet = finalCost;
        Bukkit.getScheduler().runTask(plugin, () -> {
            if (event.getView().getPlayer().getOpenInventory() == event.getView()) {
                event.getView().setRepairCost(costToSet);
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory() instanceof AnvilInventory)) {
            return;
        }
        
        if (event.getSlotType() == org.bukkit.event.inventory.InventoryType.SlotType.RESULT) {
            ItemStack result = event.getCurrentItem();
            if (result != null && result.getType() != Material.AIR) {
                if (plugin.isEffectsEnabled() && event.getWhoClicked() instanceof Player) {
                    Player player = (Player) event.getWhoClicked();
                    
                    // Only play effect if they bypassed vanilla limits. For simplicity, we just play it if they have the permission
                    // and actually crafted something, but let's play it on any successful anvil use if enabled.
                    try {
                        org.bukkit.Sound sound = org.bukkit.Sound.valueOf(plugin.getEffectSound());
                        player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
                    } catch (IllegalArgumentException ignored) {}
                    
                    try {
                        org.bukkit.Particle particle = org.bukkit.Particle.valueOf(plugin.getEffectParticles());
                        player.getWorld().spawnParticle(particle, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                    } catch (IllegalArgumentException ignored) {}
                }
            }
        }
    }

    private boolean areCompatible(ItemStack left, ItemStack right) {
        if (left == null || right == null) return false;
        if (left.getType() == Material.AIR || right.getType() == Material.AIR) return false;

        // Same type (e.g. Diamond Sword + Diamond Sword)
        if (left.getType() == right.getType()) {
            return true;
        }

        // Item + Book
        if (right.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }

        return false;
    }

    private Map<Enchantment, Integer> getEnchantments(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return Collections.emptyMap();
        }

        if (item.getType() == Material.ENCHANTED_BOOK) {
            if (item.hasItemMeta() && item.getItemMeta() instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta meta = (EnchantmentStorageMeta) item.getItemMeta();
                return meta.getStoredEnchants();
            }
        }

        return item.getEnchantments();
    }

    private void applyEnchantments(ItemStack item, Map<Enchantment, Integer> enchants) {
        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        if (item.getType() == Material.ENCHANTED_BOOK) {
            ItemMeta meta = item.getItemMeta();
            if (meta instanceof EnchantmentStorageMeta) {
                EnchantmentStorageMeta storageMeta = (EnchantmentStorageMeta) meta;
                // Clear existing
                for (Enchantment enchant : new ArrayList<>(storageMeta.getStoredEnchants().keySet())) {
                    storageMeta.removeStoredEnchant(enchant);
                }
                // Add new
                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                    storageMeta.addStoredEnchant(entry.getKey(), entry.getValue(), true);
                }
                item.setItemMeta(storageMeta);
            }
        } else {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                // Clear existing
                for (Enchantment enchant : new ArrayList<>(meta.getEnchants().keySet())) {
                    meta.removeEnchant(enchant);
                }
                item.setItemMeta(meta);
            }
            // Add new
            item.addUnsafeEnchantments(enchants);
        }
    }

    private Map<Enchantment, Integer> mergeEnchantments(ItemStack left, ItemStack right, Player player) {
        Map<Enchantment, Integer> leftEnchants = getEnchantments(left);
        Map<Enchantment, Integer> rightEnchants = getEnchantments(right);

        Map<Enchantment, Integer> merged = new HashMap<>(leftEnchants);

        for (Map.Entry<Enchantment, Integer> entry : rightEnchants.entrySet()) {
            Enchantment enchant = entry.getKey();
            int rightLevel = entry.getValue();

            if (merged.containsKey(enchant)) {
                int leftLevel = merged.get(enchant);
                int newLevel;
                if (leftLevel == rightLevel) {
                    newLevel = leftLevel + 1;
                } else {
                    newLevel = Math.max(leftLevel, rightLevel);
                }
                merged.put(enchant, clampLevel(enchant, newLevel, player));
            } else {
                // Check compatibility and conflicts
                if (isCompatible(left, enchant) && !hasConflict(merged, enchant, player)) {
                    merged.put(enchant, clampLevel(enchant, rightLevel, player));
                }
            }
        }
        return merged;
    }

    private boolean isCompatible(ItemStack item, Enchantment enchant) {
        if (item.getType() == Material.ENCHANTED_BOOK) {
            return true;
        }
        return enchant.canEnchantItem(item);
    }

    private boolean hasConflict(Map<Enchantment, Integer> currentEnchants, Enchantment enchant, Player player) {
        if (plugin.isAllowUnsafeCombinations() && player.hasPermission("limitlessenchants.bypass.conflicts")) {
            return false; // Skip conflict check completely
        }
        for (Enchantment existing : currentEnchants.keySet()) {
            if (existing != enchant && existing.conflictsWith(enchant)) {
                return true;
            }
        }
        return false;
    }

    private int clampLevel(Enchantment enchant, int level, Player player) {
        if (player.hasPermission("limitlessenchants.bypass.level")) {
            int maxLevel = getMaxEnchantmentLevel(enchant);
            return Math.min(level, maxLevel);
        }
        return Math.min(level, enchant.getMaxLevel());
    }

    private int getMaxEnchantmentLevel(Enchantment enchant) {
        NamespacedKey key = enchant.getKey();
        String fullKey = key.toString().toLowerCase();
        String keyPath = key.getKey().toLowerCase();

        Map<String, Integer> customMaxLevels = plugin.getCustomMaxLevels();

        if (customMaxLevels.containsKey(fullKey)) {
            return customMaxLevels.get(fullKey);
        }
        if (customMaxLevels.containsKey(keyPath)) {
            return customMaxLevels.get(keyPath);
        }

        if (plugin.isUseDefaultMaxForUnlisted()) {
            return plugin.getDefaultMaxLevel();
        }

        return enchant.getMaxLevel();
    }
}
