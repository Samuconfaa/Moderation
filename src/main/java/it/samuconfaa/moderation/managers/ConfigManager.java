package it.samuconfaa.moderation.managers;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.ChatColor;

import java.util.List;

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
    private String notBlacklistedMessage;
    private String reloadMessage;
    private String usageMessage;
    private String noWordMessage;
    private String blacklistedMessage;
    private String onlyPlayerMessage;
    private String noCapsMessage;
    private List<String> helpMessage;

    private int maxCaps;
    private int minLetters;
    //--------------------------------------------------------------------------------------------

    public void load() {
        plugin.reloadConfig();

        DbName = getConfigString("database.name");

        noPermissionMessage = getConfigString("messages.no-permission");
        addWordMessage = getConfigString("messages.addWord");
        removeWordMessage = getConfigString("messages.removeWord");
        notBlacklistedMessage = getConfigString("messages.notBlacklisted");
        reloadMessage = getConfigString("messages.reload");
        usageMessage = getConfigString("messages.usage");
        noWordMessage = getConfigString("messages.noWord");
        blacklistedMessage = getConfigString("messages.blacklisted");
        onlyPlayerMessage = getConfigString("messages.onlyPlayer");
        noCapsMessage = getConfigString("messages.noCaps");
        helpMessage = colorList(plugin.getConfig().getStringList("messages.help"));

        maxCaps = plugin.getConfig().getInt("caps-options.max-caps");
        minLetters = plugin.getConfig().getInt("caps-options.min-letters");

    }

    //--------------------------------------------------------------------------------------------
    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    private String getConfigString(String path) {
        return color(plugin.getConfig().getString(path));
    }

    private List<String> colorList(List<String> list) {
        return list.stream().map(this::color).toList();
    }

    //--------------------------------------------------------------------------------------------
    public String getDbName() { return DbName; }

    public String getNoPermissionMessage() { return noPermissionMessage; }
    public String getAddWordMessage() { return addWordMessage; }
    public String getRemoveWordMessage() { return removeWordMessage; }
    public String getNotBlacklistedMessage() { return notBlacklistedMessage; }
    public String getReloadMessage() { return reloadMessage; }
    public String getUsageMessage() { return usageMessage; }
    public String getNoWordMessage() { return noWordMessage; }
    public String getBlacklistedMessage() { return blacklistedMessage; }
    public String getOnlyPlayerMessage() { return onlyPlayerMessage; }
    public String getNoCapsMessage() { return noCapsMessage; }
    public List<String> getHelpMessage() { return helpMessage; }

    public int getMaxCaps() { return maxCaps; }
    public int getMinLetters() { return maxCaps; }

}
