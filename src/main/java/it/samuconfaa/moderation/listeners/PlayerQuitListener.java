package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    private Moderation plugin;
    public PlayerQuitListener(Moderation plugin) {
        this.plugin = plugin;
    }


    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        plugin.getChatCooldown().remove(p.getUniqueId());

        if(p.hasPermission("moderation.staff")){
            plugin.getStaff().remove(p);
        }
    }
}
