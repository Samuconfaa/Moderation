package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import it.samuconfaa.moderation.models.EnumAction;
import it.samuconfaa.moderation.utils.TextNormalizer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;
import java.util.Set;

public class PlayerChatListener implements Listener {
    private Moderation plugin;
    public PlayerChatListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if(player.hasPermission("moderation.bypass")){
            return;
        }
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


    private void capsFilter(AsyncPlayerChatEvent event){
        if (event.isCancelled()) return;
        String message = TextNormalizer.normalize(event.getMessage());
        Player player = event.getPlayer();
        Set<String> names = plugin.getCachedPlayerNames();

        int letters = countLetters(message);
        if (letters <= plugin.getConfigManager().getMinLetters()) {
            return;
        }

        double caps = capsPercentage(message, names);
        if (caps > plugin.getConfigManager().getMaxCaps()) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getNoCapsMessage());
            DbManager.addHistory(plugin, player.getUniqueId().toString(), message, EnumAction.CHAT_MESSAGE_CAPS);
            return;
        }

        String found = DbManager.containsBlacklistedWord(message);
        if (found != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
            sendStaffMessage(found, player);
            DbManager.addHistory(plugin, player.getUniqueId().toString(), message, EnumAction.CHAT_MESSAGE_BLACKLIST);
        }

    }

    private void sendStaffMessage(String message, Player player) {
        for(Player staff : plugin.getStaff()){
            if (staff != null && staff.isOnline()) {
                staff.sendMessage(plugin.getConfigManager().getStaffMessage().replace("%player%", player.getName()).replace("%word%", message));
                staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            }
        }
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

    private double capsPercentage(String message, Set<String> names) {
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
