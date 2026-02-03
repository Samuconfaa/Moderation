package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
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
        Location loc = event.getBlock().getLocation();
        for (int i = 0; i < event.lines().size(); i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(event.line(i));

            String found = DbManager.containsBlacklistedWordCached(line, plugin);

            if (found != null) {
                event.setCancelled(true);
                player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                sendStaffMessage(found, player, loc);
                return;
            }
        }
    }

    private void sendStaffMessage(String word, Player player, Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String pos = "X: " + x + " Y: " + y + " Z: " + z;

        String rawConfigMessage = plugin.getConfigManager().getStaffMessage()
                .replace("%player%", player.getName())
                .replace("%word%", word)
                .replace("%location%", pos);


        net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent(rawConfigMessage);
        net.md_5.bungee.api.chat.TextComponent clickPart = new net.md_5.bungee.api.chat.TextComponent(" §7§o[Click Here to TP]");

        clickPart.setClickEvent(new net.md_5.bungee.api.chat.ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/tp " + x + " " + y + " " + z
        ));

        message.addExtra(clickPart);

        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("moderation.staff"))
                .forEach(p -> {
                    p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    p.spigot().sendMessage(message);
                });
    }
}
