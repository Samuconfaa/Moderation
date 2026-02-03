package it.samuconfaa.moderation;

import it.samuconfaa.moderation.commands.ModerationCommand;
import it.samuconfaa.moderation.listeners.PlayerChatListener;
import it.samuconfaa.moderation.managers.ConfigManager;
import it.samuconfaa.moderation.managers.DbManager;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class Moderation extends JavaPlugin {

    private static Moderation instance;
    @Getter
    private ConfigManager configManager;

    @Getter
    private List<String> cachedPlayerNames;

    @Getter
    private final HashMap<UUID, Long> chatCooldown = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        configManager = new ConfigManager(this);
        configManager.load();
        getCommand("moderation").setExecutor(new ModerationCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        DbManager.init(this);
        startPlayerCacheTask();


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

    private void startPlayerCacheTask() {
        long check = getConfigManager().getIntervalCheck() *20;
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            cachedPlayerNames = Bukkit.getOnlinePlayers().stream()
                    .map(p -> p.getName().toLowerCase())
                    .collect(Collectors.toList());
        }, 0L, check);
    }

}
