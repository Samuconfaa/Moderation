package it.samuconfaa.moderation.commands;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModerationCommand implements CommandExecutor {

    private final Moderation plugin;

    public ModerationCommand(Moderation plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        if (args.length == 0) {
            for (String line : plugin.getConfigManager().getHelpMessage()) {
                sender.sendMessage(line);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        // -----------------------------------------------
        if (sub.equals("check")) {
            if (args.length < 2) {
                sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                return true;
            }

            String word = args[1];
            DbManager.isBlacklisted(word, plugin, isBlocked -> {
                if (isBlocked) {
                    sender.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                } else {
                    sender.sendMessage(plugin.getConfigManager().getNotBlacklistedMessage());
                }
            });

            return true;
        }

        // -----------------------------------------------
        if (!sender.hasPermission("moderation.admin")) {
            sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
            return true;
        }

        switch (sub) {
            case "reload":
                plugin.getConfigManager().load();
                sender.sendMessage(plugin.getConfigManager().getReloadMessage());
                break;

            case "add":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                    return true;
                }

                String addWord = args[1];
                DbManager.addWord(addWord, plugin, () ->
                        sender.sendMessage(plugin.getConfigManager().getAddWordMessage())
                );
                break;

            case "remove":
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                    return true;
                }

                String removeWord = args[1];
                DbManager.removeWord(removeWord, plugin, () ->
                        sender.sendMessage(plugin.getConfigManager().getRemoveWordMessage())
                );
                break;

            default:
                sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                break;
        }

        return true;
    }
}
