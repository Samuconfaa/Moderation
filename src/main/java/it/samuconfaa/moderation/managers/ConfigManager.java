package it.samuconfaa.moderation.managers;

import it.samuconfaa.moderation.Moderation;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.util.List;

public class ConfigManager {

    private final Moderation plugin;

    public ConfigManager(Moderation plugin) {
        this.plugin = plugin;
    }

    //--------------------------------------------------------------------------------------------
    @Getter
    private String DbName;

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
    private String noWordMessage;
    @Getter
    private String blacklistedMessage;
    @Getter
    private String onlyPlayerMessage;
    @Getter
    private String noCapsMessage;
    @Getter
    private List<String> helpMessage;

    @Getter
    private int maxCaps;
    @Getter
    private int minLetters;

    @Getter
    private long intervalCheck;
    //--------------------------------------------------------------------------------------------

    public void load() {
        plugin.reloadConfig();

        DbName = getConfigString("database.name");

        prefix = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("messages.prefix"));
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

        intervalCheck = plugin.getConfig().getLong("check-interval");

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
