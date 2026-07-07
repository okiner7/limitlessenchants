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

    private boolean allowUnsafeCombinations;
    private boolean effectsEnabled;
    private String effectSound;
    private String effectParticles;
    private String msgPrefix;
    private String msgReloadSuccess;
    private String msgNoPermission;
    private String msgHelpHeader;
    private String msgHelpReload;

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

        allowUnsafeCombinations = getConfig().getBoolean("allow-unsafe-combinations", false);
        
        effectsEnabled = getConfig().getBoolean("effects.enabled", true);
        effectSound = getConfig().getString("effects.sound", "ENTITY_PLAYER_LEVELUP");
        effectParticles = getConfig().getString("effects.particles", "ENCHANTMENT_TABLE");

        msgPrefix = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.prefix", "&e[LimitlessEnchants] &r"));
        msgReloadSuccess = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.reload-success", "&aConfiguration reloaded successfully!"));
        msgNoPermission = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.no-permission", "&cYou do not have permission to execute this command!"));
        msgHelpHeader = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.help-header", "&6=== LimitlessEnchants ==="));
        msgHelpReload = ChatColor.translateAlternateColorCodes('&', getConfig().getString("messages.help-reload", "&e/le reload &f- Reload the configuration"));
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("limitlessenchants")) {
            if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("limitlessenchants.reload")) {
                    sender.sendMessage(msgPrefix + msgNoPermission);
                    return true;
                }
                loadPluginConfig();
                sender.sendMessage(msgPrefix + msgReloadSuccess);
                return true;
            }
            // If no arguments or invalid, show help
            sender.sendMessage(msgHelpHeader);
            sender.sendMessage(msgHelpReload);
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

    public boolean isAllowUnsafeCombinations() {
        return allowUnsafeCombinations;
    }

    public boolean isEffectsEnabled() {
        return effectsEnabled;
    }

    public String getEffectSound() {
        return effectSound;
    }

    public String getEffectParticles() {
        return effectParticles;
    }
}

