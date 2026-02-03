package it.samuconfaa.moderation;

import it.samuconfaa.moderation.commands.ModerationCommand;
import it.samuconfaa.moderation.listeners.PlayerChatListener;
import it.samuconfaa.moderation.managers.ConfigManager;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Moderation extends JavaPlugin {

    private static Moderation instance;
    private ConfigManager configManager;

    @Override
    public void onEnable() {
        instance = this;

        configManager = new ConfigManager(this);
        configManager.load();
        getCommand("moderation").setExecutor(new ModerationCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        DbManager.init(this);


        getLogger().info("-------------------------------");
        getLogger().info("Moderation plugin enabled!");
        getLogger().info("-------------------------------");
    }

    @Override
    public void onDisable() {
        instance = null;
        DbManager.close();

        getLogger().info("Moderation plugin disabled!");

    }

    public ConfigManager getConfigManager(){
        return configManager;
    }
}
