package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
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
        plugin.getChatCooldown().remove(event.getPlayer().getUniqueId());
    }
}
