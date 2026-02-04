package it.samuconfaa.moderation.managers;

import it.samuconfaa.moderation.Moderation;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {

    private final Moderation plugin;
    private static final int CONFIG_VERSION = 1;

    public ConfigManager(Moderation plugin) {
        this.plugin = plugin;
    }

    //--------------------------------------------------------------------------------------------
    @Getter
    private String licenseKey;

    @Getter
    private String DbName;
    @Getter
    private int historyLimit;

    @Getter
    private String prefix;
    @Getter
    private String noPermissionMessage;
    @Getter
    private String addWordMessage;
    @Getter
    private String removeWordMessage;
    @Getter
    private String notBlacklistedMessage;
    @Getter
    private String reloadMessage;
    @Getter
    private String usageMessage;
    @Getter
    private String usageHistoryMessage;
    @Getter
    private String noWordMessage;
    @Getter
    private String blacklistedMessage;
    @Getter
    private String onlyPlayerMessage;
    @Getter
    private String noCapsMessage;
    @Getter
    private String noDelayMessage;
    @Getter
    private List<String> helpMessage;
    @Getter
    private String staffMessage;
    @Getter
    private String staffSignMessage;
    @Getter
    private String onlyIntegerMessage;

    @Getter
    private String historyHeader;
    @Getter
    private String historyFooter;
    @Getter
    private List<String> historyBody;

    @Getter
    private int maxCaps;
    @Getter
    private int minLetters;
    @Getter
    private long messageDelay;
    @Getter
    private long intervalCheck;

    //--------------------------------------------------------------------------------------------

    public void load() {
        checkAndUpdateConfig();
        plugin.reloadConfig();

        licenseKey = plugin.getConfig().getString("license-key");

        DbName = plugin.getConfig().getString("database.name");
        historyLimit = plugin.getConfig().getInt("database.history-default-limit");

        prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix"));
        noPermissionMessage = getConfigString("messages.no-permission");
        addWordMessage = getConfigString("messages.addWord");
        removeWordMessage = getConfigString("messages.removeWord");
        notBlacklistedMessage = getConfigString("messages.notBlacklisted");
        reloadMessage = getConfigString("messages.reload");
        usageMessage = getConfigString("messages.usage");
        usageHistoryMessage = getConfigString("messages.usageHistory");
        noWordMessage = getConfigString("messages.noWord");
        blacklistedMessage = getConfigString("messages.blacklisted");
        onlyPlayerMessage = getConfigString("messages.onlyPlayers");
        noCapsMessage = getConfigString("messages.noCaps");
        noDelayMessage = getConfigString("messages.noDelay");
        helpMessage = colorList(plugin.getConfig().getStringList("messages.help"));
        staffMessage = getConfigString("messages.staff-segnalation");
        staffSignMessage = getConfigString("messages.staff-sign-segnalation");
        onlyIntegerMessage = getConfigString("messages.only-integer");

        historyHeader = getConfigString("messages.player-history.header");
        historyFooter = getConfigString("messages.player-history.footer");
        historyBody = colorList(plugin.getConfig().getStringList("messages.player-history.body"));

        maxCaps = plugin.getConfig().getInt("caps-options.max-caps");
        minLetters = plugin.getConfig().getInt("caps-options.min-letters");
        messageDelay = (long) plugin.getConfig().getInt("message-delay") * 1000L;
        intervalCheck = plugin.getConfig().getLong("check-interval");

    }


    private void checkAndUpdateConfig() {
        int currentVersion = plugin.getConfig().getInt("config-version", 0);

        if (currentVersion != CONFIG_VERSION) {
            plugin.getLogger().warning("╔════════════════════════════════════════════╗");
            plugin.getLogger().warning("║  CONFIG OUTDATED - Updating...             ║");
            plugin.getLogger().warning("║  Old version: " + currentVersion + " → New version: " + CONFIG_VERSION + "       ║");
            plugin.getLogger().warning("╚════════════════════════════════════════════╝");

            backupConfig();
            migrateConfig(currentVersion);

            plugin.getConfig().getKeys(false).forEach(key ->
                    plugin.getConfig().set(key, null)
            );

            plugin.saveDefaultConfig();
            plugin.reloadConfig();

            plugin.getLogger().info("✓ Config updated successfully!");
            plugin.getLogger().info("✓ Old config backed up to config.yml.old");
        }
    }

    private void backupConfig() {
        try {
            java.io.File configFile = new java.io.File(plugin.getDataFolder(), "config.yml");
            java.io.File backupFile = new java.io.File(plugin.getDataFolder(), "config.yml.old");

            if (configFile.exists()) {
                java.nio.file.Files.copy(
                        configFile.toPath(),
                        backupFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING
                );
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to backup config: " + e.getMessage());
        }
    }

    private void migrateConfig(int fromVersion) {
        Map<String, Object> userValues = new HashMap<>();

        List<String> preserveKeys = List.of(
                "license-key",
                "database.name",
                "database.history-default-limit",
                "check-interval",
                "message-delay",
                "caps-options.max-caps",
                "caps-options.min-letters"
        );

        for (String key : preserveKeys) {
            if (plugin.getConfig().contains(key)) {
                userValues.put(key, plugin.getConfig().get(key));
            }
        }

        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        for (Map.Entry<String, Object> entry : userValues.entrySet()) {
            plugin.getConfig().set(entry.getKey(), entry.getValue());
        }

        plugin.getConfig().set("config-version", CONFIG_VERSION);
        plugin.saveConfig();

        plugin.getLogger().info("Preserved " + userValues.size() + " user settings");
    }

    //--------------------------------------------------------------------------------------------
    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private String getConfigString(String path) {
        String value = plugin.getConfig().getString(path, "");
        return prefix + ChatColor.translateAlternateColorCodes('&', value);
    }

    private List<String> colorList(List<String> list) {
        return list.stream().map(this::color).toList();
    }
}