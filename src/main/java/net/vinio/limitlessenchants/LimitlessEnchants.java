package net.vinio.limitlessenchants;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bstats.bukkit.Metrics;

import java.io.File;
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

    private FileConfiguration messages;
    private ConfigGUI configGUI;

    @Override
    public void onEnable() {
        // Save default config if not exists
        saveDefaultConfig();
        
        // Save language files
        saveResource("messages_en.yml", false);
        saveResource("messages_ru.yml", false);

        // Load configuration values
        loadPluginConfig();

        // Register listener
        getServer().getPluginManager().registerEvents(new AnvilListener(this), this);

        // Register GUI listener
        configGUI = new ConfigGUI(this);
        getServer().getPluginManager().registerEvents(configGUI, this);

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

        String lang = getConfig().getString("language", "en");
        File langFile = new File(getDataFolder(), "messages_" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(getDataFolder(), "messages_en.yml");
        }
        messages = YamlConfiguration.loadConfiguration(langFile);
    }

    public String getMessage(String key) {
        if (messages == null) return key;
        String msg = messages.getString(key, key);
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getMessage(String key, String... replacements) {
        String msg = getMessage(key);
        for (int i = 0; i < replacements.length; i++) {
            msg = msg.replace("{" + i + "}", replacements[i]);
        }
        return msg;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("limitlessenchants")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!sender.hasPermission("limitlessenchants.reload")) {
                        sender.sendMessage(getMessage("prefix") + getMessage("no-permission"));
                        return true;
                    }
                    loadPluginConfig();
                    sender.sendMessage(getMessage("prefix") + getMessage("reload-success"));
                    return true;
                }
                if (args[0].equalsIgnoreCase("lang") && args.length == 2) {
                    if (!sender.hasPermission("limitlessenchants.reload")) {
                        sender.sendMessage(getMessage("prefix") + getMessage("no-permission"));
                        return true;
                    }
                    String newLang = args[1].toLowerCase();
                    if (newLang.equals("en") || newLang.equals("ru")) {
                        getConfig().set("language", newLang);
                        saveConfig();
                        loadPluginConfig();
                        sender.sendMessage(getMessage("prefix") + getMessage("lang-success"));
                    } else {
                        sender.sendMessage(getMessage("prefix") + getMessage("lang-invalid"));
                    }
                    return true;
                }
            }
            
            // Open GUI for players
            if (sender instanceof Player) {
                Player player = (Player) sender;
                if (player.hasPermission("limitlessenchants.use")) {
                    configGUI.openMainMenu(player);
                    return true;
                } else {
                    player.sendMessage(getMessage("prefix") + getMessage("no-permission"));
                    return true;
                }
            }

            // If no arguments or invalid (Console fallback)
            sender.sendMessage(getMessage("help-header"));
            sender.sendMessage(getMessage("help-reload"));
            sender.sendMessage(getMessage("help-lang"));
            return true;
        }
        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (command.getName().equalsIgnoreCase("limitlessenchants")) {
            List<String> completions = new ArrayList<>();
            if (args.length == 1) {
                if ("reload".startsWith(args[0].toLowerCase()) && sender.hasPermission("limitlessenchants.reload")) {
                    completions.add("reload");
                }
                if ("lang".startsWith(args[0].toLowerCase()) && sender.hasPermission("limitlessenchants.reload")) {
                    completions.add("lang");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("lang") && sender.hasPermission("limitlessenchants.reload")) {
                if ("en".startsWith(args[1].toLowerCase())) completions.add("en");
                if ("ru".startsWith(args[1].toLowerCase())) completions.add("ru");
            }
            return completions;
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
