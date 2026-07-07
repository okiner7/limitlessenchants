package net.vinio.limitlessenchants;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LimitlessEnchants extends JavaPlugin {

    private int maxRepairCost;
    private int defaultMaxLevel;
    private boolean useDefaultMaxForUnlisted;
    private final Map<String, Integer> customMaxLevels = new HashMap<>();

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        
        // Load configuration values
        loadPluginConfig();

        // Register listener
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);

        // Initialize bStats Metrics
        int pluginId = 31895;
        new Metrics(this, pluginId);

        // Register command executor
        if (getCommand("limitlessenchants") != null) {
            getCommand("limitlessenchants").setExecutor(this);
            getCommand("limitlessenchants").setTabCompleter(this);
        }
        
        getLogger().info("LimitlessEnchants has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("LimitlessEnchants has been disabled!");
    }

    public void loadPluginConfig() {
        reloadConfig();
        customMaxLevels.clear();
        
        maxRepairCost = getConfig().getInt("max-repair-cost", 39);
        defaultMaxLevel = getConfig().getInt("default-max-level", 10);
        useDefaultMaxForUnlisted = getConfig().getBoolean("use-default-max-for-unlisted", false);
        
        if (getConfig().isConfigurationSection("enchantments")) {
            for (String key : getConfig().getConfigurationSection("enchantments").getKeys(false)) {
                int maxLevel = getConfig().getInt("enchantments." + key);
                // Store both lowercase key and namespace version if applicable
                customMaxLevels.put(key.toLowerCase(), maxLevel);
                if (!key.contains(":")) {
                    customMaxLevels.put("minecraft:" + key.toLowerCase(), maxLevel);
                }
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("limitlessenchants")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("limitlessenchants.reload")) {
                    sender.sendMessage(ChatColor.RED + "You do not have permission to execute this command!");
                    return true;
                }
                loadPluginConfig();
                sender.sendMessage(ChatColor.GREEN + "[LimitlessEnchants] Configuration reloaded successfully!");
                return true;
            }
            // If no arguments or invalid, show help
            sender.sendMessage(ChatColor.GOLD + "=== LimitlessEnchants ===");
            sender.sendMessage(ChatColor.YELLOW + "/le reload " + ChatColor.WHITE + "- Reload the configuration");
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("limitlessenchants")) {
            if (args.length == 1) {
                List<String> completions = new ArrayList<>();
                if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("limitlessenchants.reload")) {
                    completions.add("reload");
                }
                return completions;
            }
        }
        return Collections.emptyList();
    }

    public int getMaxRepairCost() {
        return maxRepairCost;
    }

    public int getDefaultMaxLevel() {
        return defaultMaxLevel;
    }

    public boolean isUseDefaultMaxForUnlisted() {
        return useDefaultMaxForUnlisted;
    }

    public Map<String, Integer> getCustomMaxLevels() {
        return customMaxLevels;
    }
}

