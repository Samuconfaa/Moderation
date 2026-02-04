package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import it.samuconfaa.moderation.models.EnumAction;
import it.samuconfaa.moderation.utils.TextNormalizer;
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
import java.util.UUID;
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
        StringBuilder fullText = new StringBuilder();
        for (int i = 0; i < event.lines().size(); i++) {
            String line = PlainTextComponentSerializer.plainText().serialize(event.line(i));
            fullText.append(line).append(" ");
        }
        String text = TextNormalizer.normalize(fullText.toString());
        String found = DbManager.containsBlacklistedWordCached(text, plugin);
        if (found != null) {
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
            DbManager.addHistory(plugin, player.getUniqueId().toString(), text, EnumAction.SIGN_MESSAGE);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                sendStaffMessage(found, player, loc);
            });
        }

    }

    private void sendStaffMessage(String word, Player player, Location loc) {
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String pos = "X: " + x + " Y: " + y + " Z: " + z;

        String rawConfigMessage = plugin.getConfigManager().getStaffSignMessage()
                .replace("%player%", player.getName())
                .replace("%word%", word)
                .replace("%location%", "X: " + x + " Y: " + y + " Z: " + z);


        net.md_5.bungee.api.chat.TextComponent message = new net.md_5.bungee.api.chat.TextComponent(rawConfigMessage);
        net.md_5.bungee.api.chat.TextComponent clickPart = new net.md_5.bungee.api.chat.TextComponent(" §7§o[Click Here to TP]");

        clickPart.setClickEvent(new ClickEvent(
                ClickEvent.Action.RUN_COMMAND,
                "/tp " + x + " " + y + " " + z
        ));

        message.addExtra(clickPart);

        List<UUID> staffUUIDs = Bukkit.getOnlinePlayers().stream()
                .map(Player::getUniqueId)
                .collect(Collectors.toList());

        Bukkit.getScheduler().runTask(plugin, () -> {
            for(Player staff : plugin.getStaff()){
                if (staff != null && staff.isOnline()) {
                    staff.sendMessage(message);
                    staff.playSound(staff.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
                }
            }
        });
    }
}
