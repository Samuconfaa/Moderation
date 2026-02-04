package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


public class PlayerJoinListener implements Listener {
    private Moderation plugin;
    public PlayerJoinListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event){
        Player p = event.getPlayer();
        plugin.getCachedPlayerNames().add(p.getName());
        if(p.hasPermission("moderation.admin")){
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if(plugin.isUpdate()){
                    p.sendMessage("§6§l[Moderation] §eA new version of the plugin is available!");
                }
            }, 60L);
        }

        if(p.hasPermission("moderation.staff")){
            plugin.getStaff().add(p);
        }
    }
}
