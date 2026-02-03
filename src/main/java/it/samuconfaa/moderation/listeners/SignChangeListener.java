package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

import java.util.List;
import java.util.stream.Collectors;

public class SignChangeListener implements Listener {
    private Moderation plugin;
    public SignChangeListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {
        Player player = event.getPlayer();
        String[] lines = event.lines().toArray(new String[0]);

        for (String line : lines) {
            String found = DbManager.containsBlacklistedWordCached(line, plugin);

            if (found != null) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                sendStaffMessage(found, player);
                return;
            }
        }
    }

    private void sendStaffMessage(String word, Player player) {
        List<Player> staff = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("moderation.staff"))
                .collect(Collectors.toList());
        staff.forEach(p -> p.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f));
        staff.forEach(p -> p.sendMessage(plugin.getConfigManager().getStaffMessage().replace("%player%", player.getName()).replace("%word%", word)));
    }
}
