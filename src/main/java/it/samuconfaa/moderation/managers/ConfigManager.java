package it.samuconfaa.moderation.managers;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.ChatColor;

public class ConfigManager {

    private final Moderation plugin;

    public ConfigManager(Moderation plugin) {
        this.plugin = plugin;
    }

    //--------------------------------------------------------------------------------------------
    private String DbName;

    private String noPermissionMessage;
    private String addWordMessage;
    private String removeWordMessage;
    private String alreadyBlacklistedMessage;
    private String notBlacklistedMessage;
    private String reloadMessage;
    private String usageMessage;
    private String noWordMessage;
    private String blacklistedMessage;
    private String onlyPlayerMessage;

    //--------------------------------------------------------------------------------------------

    public void load() {
        plugin.reloadConfig();

        DbName = getConfigString("database.name");

        noPermissionMessage = getConfigString("messages.no-permission");
        addWordMessage = getConfigString("messages.addWord");
        removeWordMessage = getConfigString("messages.removeWord");
        alreadyBlacklistedMessage = getConfigString("messages.alreadyBlacklisted");
        notBlacklistedMessage = getConfigString("messages.notBlacklisted");
        reloadMessage = getConfigString("messages.reload");
        usageMessage = getConfigString("messages.usage");
        noWordMessage = getConfigString("messages.noWord");
        blacklistedMessage = getConfigString("messages.blacklisted");
        onlyPlayerMessage = getConfigString("messages.onlyPlayer");

    }

    //--------------------------------------------------------------------------------------------
    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private String getConfigString(String path) {
        return color(plugin.getConfig().getString(path));
    }

    //--------------------------------------------------------------------------------------------
    public String getDbName() { return DbName; }

    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getAddWordMessage() { return addWordMessage; }
    public String getRemoveWordMessage() { return removeWordMessage; }
    public String getAlreadyBlacklistedMessage() { return alreadyBlacklistedMessage; }
    public String getNotBlacklistedMessage() { return notBlacklistedMessage; }
    public String getReloadMessage() { return reloadMessage; }
    public String getUsageMessage() { return usageMessage; }
    public String getNoWordMessage() { return noWordMessage; }
    public String getBlacklistedMessage() { return blacklistedMessage; }
    public String getOnlyPlayerMessage() { return onlyPlayerMessage; }

}
