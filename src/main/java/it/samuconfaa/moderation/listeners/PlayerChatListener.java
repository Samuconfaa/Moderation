package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.function.Consumer;

public class PlayerChatListener implements Listener {
    private Moderation plugin;
    public PlayerChatListener(Moderation plugin) {
        this.plugin = plugin;
    }



    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        String message = event.getMessage();
        Player player = event.getPlayer();
        int letters = countLetters(message);
        if (letters <= plugin.getConfigManager().getMinLetters()) {
            return;
        }

        List<String> names = Bukkit.getOnlinePlayers()
                .stream()
                .map(p -> p.getName().toLowerCase())
                .toList();
        double caps = capsPercentage(message, names);
        if (caps > plugin.getConfigManager().getMaxCaps()) {
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (event.isCancelled()) return;
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getNoCapsMessage());
            });
            return;
        }

        DbManager.containsBlacklistedWordCached(message, plugin, found -> {
            if (found) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (event.isCancelled()) return;
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                });
            }
        });
    }

    private int countLetters(String message) {
        int letters = 0;
        for (char c : message.toCharArray()) {
            if (Character.isLetter(c)) {
                letters++;
            }
        }
        return letters;
    }

    private double capsPercentage(String message, List<String> names) {

        String clean = message.toLowerCase();
        for (String name : names) {
            clean = clean.replace(name, "");
        }

        int caps = 0;
        int letters = 0;

        for (int i = 0; i < message.length(); i++) {
            char original = message.charAt(i);
            char filtered = clean.charAt(i);

            if (Character.isLetter(filtered)) {
                letters++;
                if (Character.isUpperCase(original)) {
                    caps++;
                }
            }
        }

        return letters == 0 ? 0 : (caps * 100.0) / letters;
    }


}
