package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class PlayerChatListener implements Listener {
    private Moderation plugin;
    public PlayerChatListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String messaggio = event.getMessage();
        DbManager.containsBlacklistedWordCached(messaggio, plugin, found -> {
            if (found) {
                event.getPlayer().sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                event.setCancelled(true);
            }
        });
    }
}
