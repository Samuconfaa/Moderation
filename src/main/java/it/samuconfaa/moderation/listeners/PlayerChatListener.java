package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.stream.Collectors;

public class PlayerChatListener implements Listener {
    private Moderation plugin;
    public PlayerChatListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        capsFilter(event);
        if (!event.isCancelled()) {
            checkTime(event);
        }
    }

    private void checkTime(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        long now = System.currentTimeMillis();

        if (plugin.getChatCooldown().containsKey(player.getUniqueId())) {
            long millis = plugin.getChatCooldown().get(player.getUniqueId());
            long delay = plugin.getConfigManager().getMessageDelay();
            long diff = now - millis;
            if(diff < delay){
                int seconds = (int) (delay - diff) / 1000;
                player.sendMessage(plugin.getConfigManager().getNoDelayMessage().replace("%time%", seconds+""));
                event.setCancelled(true);
                return;
            }
        }

        plugin.getChatCooldown().put(player.getUniqueId(), now);
    }

    private String serializeText(String line) {
        return line.toLowerCase()
                .replace("4", "a")
                .replace("3", "e")
                .replace("1", "i")
                .replace("0", "o")
                .replace("5", "s")
                .replace("7", "t")
                .replace("@", "a");
    }

    private void capsFilter(AsyncPlayerChatEvent event){
        if (event.isCancelled()) return;
        String message = serializeText(event.getMessage());
        Player player = event.getPlayer();
        List<String> names = plugin.getCachedPlayerNames();

        int letters = countLetters(message);
        if (letters <= plugin.getConfigManager().getMinLetters()) {
            return;
        }

        double caps = capsPercentage(message, names);
        if (caps > plugin.getConfigManager().getMaxCaps()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getNoCapsMessage());
            return;
        }

        String found = DbManager.containsBlacklistedWordCached(message, plugin);
        if (found != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
            sendStaffMessage(found, player);
        }

    }

    private void sendStaffMessage(String message, Player player) {
        List<Player> staff = Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("moderation.staff"))
                .collect(Collectors.toList());
        staff.forEach(p -> p.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f));
        staff.forEach(p -> p.sendMessage(plugin.getConfigManager().getStaffMessage().replace("%player%", player.getName()).replace("%word%", message)));
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
        boolean[] ignore = new boolean[message.length()];

        String lowerMsg = message.toLowerCase();
        for (String name : names) {
            int index = 0;
            while ((index = lowerMsg.indexOf(name, index)) != -1) {
                for (int i = index; i < index + name.length() && i < ignore.length; i++) {
                    ignore[i] = true;
                }
                index += name.length();
            }
        }

        int caps = 0;
        int letters = 0;

        for (int i = 0; i < message.length(); i++) {
            if (!ignore[i] && Character.isLetter(message.charAt(i))) {
                letters++;
                if (Character.isUpperCase(message.charAt(i))) {
                    caps++;
                }
            }
        }

        return letters == 0 ? 0 : (caps * 100.0) / letters;
    }
}
