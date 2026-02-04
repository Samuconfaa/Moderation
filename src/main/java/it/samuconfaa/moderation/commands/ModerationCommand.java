package it.samuconfaa.moderation.commands;

import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.managers.DbManager;
import it.samuconfaa.moderation.models.DbSegnalationModel;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ModerationCommand implements CommandExecutor, TabCompleter {

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
            if (sender.hasPermission("moderation.check")) {
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                    return true;
                }
            } else {
                sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }

            String word = args[1];
            boolean isBlocked = DbManager.isBlacklisted(word);
            if (isBlocked) {
                sender.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
            } else {
                sender.sendMessage(plugin.getConfigManager().getNotBlacklistedMessage());
            }

            return true;
        }

        // -----------------------------------------------


        switch (sub) {
            case "reload":
                if (!sender.hasPermission("moderation.reload")) {
                    sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                    return true;
                }
                plugin.getConfigManager().load();
                sender.sendMessage(plugin.getConfigManager().getReloadMessage());
                break;

            case "add":
                if (!sender.hasPermission("moderation.add")) {
                    sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                    return true;
                }

                String addWord = args[1].toLowerCase().trim();

                // Validazione
                if (addWord.isEmpty()) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cWord cannot be empty!");
                    return true;
                }

                if (addWord.length() > 50) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cWord too long (max 50 chars)!");
                    return true;
                }

                if (!addWord.matches("[a-z0-9]+")) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cWord can only contain letters and numbers!");
                    return true;
                }

                DbManager.addWordToBlacklist(addWord, plugin, () ->
                        sender.sendMessage(plugin.getConfigManager().getAddWordMessage())
                );
                break;

            case "remove":
                if (!sender.hasPermission("moderation.remove")) {
                    sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                    return true;
                }

                String removeWord = args[1];
                DbManager.removeWordFromBlacklist(removeWord, plugin, () ->
                        sender.sendMessage(plugin.getConfigManager().getRemoveWordMessage())
                );
                break;

            case "history":
                if (!sender.hasPermission("moderation.history")) {
                    sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                    return true;
                }
                String playerName;
                int limit = 0;

                if (args.length == 2) {
                    playerName = args[1];
                } else if (args.length == 3) {
                    playerName = args[1];
                    try {
                        limit = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getConfigManager().getOnlyIntegerMessage());
                        return true;
                    }

                } else {
                    playerName = "";
                    sender.sendMessage(plugin.getConfigManager().getUsageHistoryMessage());
                    return true;
                }

                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
                String playerUUID = offlinePlayer.getUniqueId().toString();

                int finalLimit = limit;
                DbManager.getHistory(plugin, playerUUID, finalLimit, history -> {
                    sender.sendMessage(plugin.getConfigManager().getHistoryHeader().replace("%player%", playerName));

                    if (history.isEmpty()) {
                        sender.sendMessage(plugin.getConfigManager().getPrefix() + "§7No history found for this player.");
                    } else {
                        for (DbSegnalationModel model : history) {
                            for (String line : plugin.getConfigManager().getHistoryBody()) {
                                sender.sendMessage(line
                                        .replace("%player%", playerName)
                                        .replace("%message%", model.Message)
                                        .replace("%date%", model.Timestamp.toString())
                                        .replace("%action%", model.Action.toString()));
                            }
                        }
                    }
                    sender.sendMessage(plugin.getConfigManager().getHistoryFooter());
                });
                break;

            default:
                sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                break;
        }

        return true;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        if (args.length == 1) {
            List<String> completions = new ArrayList<>();

            List<String> commands = List.of("reload", "add", "remove", "check", "history");

            List<String> allowed = new ArrayList<>();
            for (String cmd : commands) {
                switch (cmd) {
                    case "reload" ->{
                        if (sender.hasPermission("moderation.reload")) allowed.add(cmd);
                    }
                    case "add" ->{
                        if (sender.hasPermission("moderation.add")) allowed.add(cmd);
                    }
                    case "remove" ->{
                        if (sender.hasPermission("moderation.remove")) allowed.add(cmd);
                    }
                    case "history" -> {
                        if (sender.hasPermission("moderation.history")) allowed.add(cmd);
                    }
                    case "check" -> {
                        if (sender.hasPermission("moderation.check")) allowed.add(cmd);
                    }
                }
            }

            org.bukkit.util.StringUtil.copyPartialMatches(args[0], allowed, completions);
            completions.sort(String::compareToIgnoreCase);
            return completions;
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("history")) {
            if (sender.hasPermission("moderation.admin")) {
                List<String> completions = new ArrayList<>();
                List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                        .map(org.bukkit.entity.Player::getName)
                        .toList();

                org.bukkit.util.StringUtil.copyPartialMatches(args[1], playerNames, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("history")) {
            if (sender.hasPermission("moderation.admin")) {
                return List.of("5", "10", "20", "50", "100");
            }
        }

        return List.of();
    }
}
