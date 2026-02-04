package it.samuconfaa.moderation.managers;

import it.samuconfaa.moderation.Moderation;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
public class ConfigManager {

    private final Moderation plugin;
    private static final int CONFIG_VERSION = 1;

    public ConfigManager(Moderation plugin) {
        this.plugin = plugin;
    }

    //--------------------------------------------------------------------------------------------
    private String licenseKey;

    private String DbName;
    private int historyLimit;
    private int maxWordCharacter;

    private long backupDelay;
    private String defaultExtension;
    private String exportUsageMessage;
    private String typeErrorMessage;
    private String formatErrorMessage;
    private String exportingMessage;
    private String exportedMessage;
    private String exportErrorMessage;
    private String importUsageMessage;
    private String modeErrorMessage;
    private String fileNotFoundMessage;
    private String fileSuggestMessage;
    private String importingMessage;
    private String importedMessage;
    private String importErrorMessage;

    private String prefix;
    private String noPermissionMessage;
    private String addWordToBlacklistMessage;
    private String removeWordFromBlacklistMessage;
    private String addWordToWhitelistMessage;
    private String removeWordFromWhitelistMessage;

    private String reloadMessage;
    private String usageMessage;
    private String usageHistoryMessage;
    private String noWordMessage;
    private String blacklistedMessage;
    private String notBlacklistedMessage;
    private String whitelistedMessage;
    private String notWhitelistedMessage;
    private String onlyPlayerMessage;
    private String noCapsMessage;
    private String noDelayMessage;
    private List<String> helpMessage;
    private String staffMessage;
    private String staffSignMessage;
    private String onlyIntegerMessage;
    private String noEmptyWordsMessage;
    private String wordTooLongMessage;
    private String onlyLettersNumbersMessage;

    private String historyHeader;
    private String historyFooter;
    private List<String> historyBody;

    private int maxCaps;
    private int minLetters;
    private long messageDelay;
    private long intervalCheck;

    //--------------------------------------------------------------------------------------------

    public void load() {
        checkAndUpdateConfig();
        plugin.reloadConfig();

        licenseKey = plugin.getConfig().getString("license-key");

        DbName = plugin.getConfig().getString("database.name");
        historyLimit = plugin.getConfig().getInt("database.history-default-limit");
        maxWordCharacter = plugin.getConfig().getInt("database.max-word-length");

        backupDelay = plugin.getConfig().getLong("import-export.backup-delay") * 60 * 20;
        defaultExtension = plugin.getConfig().getString("import-export.default-extension", "txt").toLowerCase();
        exportUsageMessage = color(plugin.getConfig().getString("import-export.export-usage"));
        typeErrorMessage = color(plugin.getConfig().getString("import-export.type-error"));
        formatErrorMessage = color(plugin.getConfig().getString("import-export.format-error"));
        exportingMessage = color(plugin.getConfig().getString("import-export.exporting-message"));
        exportedMessage = color(plugin.getConfig().getString("import-export.exported-message"));
        exportErrorMessage = color(plugin.getConfig().getString("import-export.export-error"));
        importUsageMessage = color(plugin.getConfig().getString("import-export.import-usage"));
        modeErrorMessage = color(plugin.getConfig().getString("import-export.mode-error"));
        fileNotFoundMessage = color(plugin.getConfig().getString("import-export.file-not-found"));
        fileSuggestMessage = color(plugin.getConfig().getString("import-export.file-suggest"));
        importingMessage = color(plugin.getConfig().getString("import-export.importing-message"));
        importedMessage = color(plugin.getConfig().getString("import-export.imported-message"));
        importErrorMessage = color(plugin.getConfig().getString("import-export.import-error"));

        prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix"));
        noPermissionMessage = getConfigString("messages.no-permission");
        addWordToBlacklistMessage = getConfigString("messages.addWordToBlacklist");
        removeWordFromBlacklistMessage = getConfigString("messages.removeWordFromBlacklist");
        addWordToWhitelistMessage = getConfigString("messages.addWordToWhitelist");
        removeWordFromWhitelistMessage = getConfigString("messages.removeWordFromWhitelist");
        reloadMessage = getConfigString("messages.reload");
        usageMessage = getConfigString("messages.usage");
        usageHistoryMessage = getConfigString("messages.usageHistory");
        noWordMessage = getConfigString("messages.noWord");
        blacklistedMessage = getConfigString("messages.blacklisted");
        notBlacklistedMessage = getConfigString("messages.notBlacklisted");
        whitelistedMessage = getConfigString("messages.whitelisted");
        notWhitelistedMessage = getConfigString("messages.notWhitelisted");

        onlyPlayerMessage = getConfigString("messages.onlyPlayers");
        noCapsMessage = getConfigString("messages.noCaps");
        noDelayMessage = getConfigString("messages.noDelay");
        helpMessage = colorList(plugin.getConfig().getStringList("messages.help"));
        staffMessage = getConfigString("messages.staff-segnalation");
        staffSignMessage = getConfigString("messages.staff-sign-segnalation");
        onlyIntegerMessage = getConfigString("messages.only-integer");
        noEmptyWordsMessage = getConfigString("messages.noEmptyWords");
        wordTooLongMessage = getConfigString("messages.wordTooLong");
        onlyLettersNumbersMessage = getConfigString("messages.onlyLettersNumbers");

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
        if (msg == null) return "";
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