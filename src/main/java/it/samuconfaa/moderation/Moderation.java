package it.samuconfaa.moderation;

import it.samuconfaa.moderation.commands.ModerationCommand;
import it.samuconfaa.moderation.listeners.PlayerChatListener;
import it.samuconfaa.moderation.listeners.PlayerJoinListener;
import it.samuconfaa.moderation.listeners.PlayerQuitListener;
import it.samuconfaa.moderation.listeners.SignChangeListener;
import it.samuconfaa.moderation.managers.ConfigManager;
import it.samuconfaa.moderation.managers.DbManager;
import it.samuconfaa.moderation.managers.ImportExportManager;
import it.samuconfaa.moderation.managers.LicenseManager;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


public final class Moderation extends JavaPlugin {
    @Getter
    private static Moderation instance;

    @Getter
    private ConfigManager configManager;

    @Getter
    private ImportExportManager importExportManager;

    @Getter
    private final Set<String> cachedPlayerNames = ConcurrentHashMap.newKeySet();

    @Getter
    private final ConcurrentHashMap<UUID, Long> chatCooldown = new ConcurrentHashMap<>();

    @Getter
    private final Set<Player> staff = ConcurrentHashMap.newKeySet();

    @Getter
    @Setter
    public boolean update = false;

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        configManager.load();

        importExportManager = new ImportExportManager(this);

        createDir();
        String pluginName = "Moderation";
        String rawKey = getConfigManager().getLicenseKey();
        if (rawKey == null || rawKey.isEmpty()) {
            getLogger().severe("config.yml doesn't have a license!");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        String key = rawKey.trim();

        new LicenseManager(this, pluginName, key).check();

        getCommand("moderation").setExecutor(new ModerationCommand(this));
        getServer().getPluginManager().registerEvents(new PlayerChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(this), this);
        getServer().getPluginManager().registerEvents(new SignChangeListener(this), this);

        DbManager.init(this);

        startCooldownCleanupTask();
        startBackupTask();


        getLogger().info("-------------------------------");
        getLogger().info("Moderation plugin enabled!");
        getLogger().info("-------------------------------");
    }

    @Override
    public void onDisable() {
        instance = null;
        Bukkit.getScheduler().cancelTasks(this);

        DbManager.close();
        chatCooldown.clear();
        staff.clear();
        cachedPlayerNames.clear();

        getLogger().info("Moderation plugin disabled!");
    }

    private void startBackupTask(){
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () ->{
            String def = getConfigManager().getDefaultExtension();
            if(!(def.equals("txt") || def.equals("json"))) {
                def = "txt";
                getLogger().warning("Default extension not set or set incorrectly. Now is " + def);
            }
            if(def.equals("txt")){
                getImportExportManager().exportBlacklistTXT(null);
                getImportExportManager().exportWhitelistTXT(null);
            }else if(def.equals("json")){
                getImportExportManager().exportBlacklistJSON(null);
                getImportExportManager().exportWhitelistJSON(null);
            }
        }, 0L, getConfigManager().getBackupDelay());
    }

    private void createDir() {
        File importsFolder = new File(getDataFolder(), "imports");
        File exportsFolder = new File(getDataFolder(), "exports");
        if (!importsFolder.exists()) {
            importsFolder.mkdirs();
            getLogger().info("Created imports folder: " + importsFolder.getAbsolutePath());
        }
        if (!exportsFolder.exists()) {
            exportsFolder.mkdirs();
            getLogger().info("Created exports folder: " + exportsFolder.getAbsolutePath());
        }
    }

    private void startCooldownCleanupTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            long now = System.currentTimeMillis();
            long maxAge = 60000 * 10;

            chatCooldown.entrySet().removeIf(entry ->
                    now - entry.getValue() > maxAge
            );

            getLogger().info("Cleaned up cooldown map. Size: " + chatCooldown.size());
        }, 6000L, 6000L);
    }

}