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

import java.io.File;
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

        if (sub.equals("export")) {
            if (!sender.hasPermission("moderation.export")) {
                sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }

            if (args.length != 3) {
                sender.sendMessage(plugin.getConfigManager().getExportErrorMessage());
                return true;
            }

            String type = args[1].toLowerCase();
            String format = args[2].toLowerCase();

            if (!type.equals("blacklist") && !type.equals("whitelist")) {
                sender.sendMessage(plugin.getConfigManager().getTypeErrorMessage());
                return true;
            }

            if (!format.equals("json") && !format.equals("txt")) {
                sender.sendMessage(plugin.getConfigManager().getFormatErrorMessage());
                return true;
            }

            sender.sendMessage(plugin.getConfigManager().getExportingMessage().replace("%format%", format.toUpperCase()).replace("%type%", type));

            if (type.equals("blacklist")) {
                if (format.equals("json")) {
                    plugin.getImportExportManager().exportBlacklistJSON((success, file, count, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getExportedMessage().replace("%count%", count+"").replace("%file%", file.getName()));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getExportErrorMessage().replace("%error%", error));
                        }
                    });
                } else {
                    plugin.getImportExportManager().exportBlacklistTXT((success, file, count, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getExportedMessage().replace("%count%", count+"").replace("%file%", file.getName()));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getExportErrorMessage().replace("%error%", error));
                        }
                    });
                }
            } else {
                if (format.equals("json")) {
                    plugin.getImportExportManager().exportWhitelistJSON((success, file, count, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getExportedMessage().replace("%count%", count+"").replace("%file%", file.getName()));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getExportErrorMessage().replace("%error%", error));
                        }
                    });
                } else {
                    plugin.getImportExportManager().exportWhitelistTXT((success, file, count, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getExportedMessage().replace("%count%", count+"").replace("%file%", file.getName()));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getExportErrorMessage().replace("%error%", error));
                        }
                    });
                }
            }
            return true;
        }

        // -----------------------------------------------
        if (sub.equals("import")) {
            if (!sender.hasPermission("moderation.import")) {
                sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }

            if (args.length < 4 || args.length > 5) {
                sender.sendMessage(plugin.getConfigManager().getImportUsageMessage());
                return true;
            }

            String type = args[1].toLowerCase();
            String format = args[2].toLowerCase();
            String filename = args[3];
            boolean merge = true;

            if (args.length == 5) {
                String mode = args[4].toLowerCase();
                if (mode.equals("replace")) {
                    merge = false;
                } else if (!mode.equals("merge")) {
                    sender.sendMessage(plugin.getConfigManager().getPrefix() + "§cMode must be: merge or replace");
                    return true;
                }
            }

            if (!type.equals("blacklist") && !type.equals("whitelist")) {
                sender.sendMessage(plugin.getConfigManager().getTypeErrorMessage());
                return true;
            }

            if (!format.equals("json") && !format.equals("txt")) {
                sender.sendMessage(plugin.getConfigManager().getFormatErrorMessage());
                return true;
            }

            File file = new File(plugin.getDataFolder(), "imports/" + filename);
            if (!file.exists()) {
                sender.sendMessage(plugin.getConfigManager().getFileNotFoundMessage().replace("%file%", filename));
                sender.sendMessage(plugin.getConfigManager().getFileSuggestMessage().replace("%path%", plugin.getDataFolder().getAbsolutePath()));
                return true;
            }

            String modeText = merge ? "merge" : "replace";
            sender.sendMessage(plugin.getConfigManager().getImportingMessage().replace("type", type).replace("%file%",filename).replace("%mode%", modeText));

            if (type.equals("blacklist")) {
                if (format.equals("json")) {
                    plugin.getImportExportManager().importBlacklistJSON(file, merge, (success, added, skipped, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getImportedMessage().replace("%count%", added+"").replace("%skipped%", skipped+""));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getImportErrorMessage().replace("%error%", error));
                        }
                    });
                } else {
                    plugin.getImportExportManager().importBlacklistTXT(file, merge, (success, added, skipped, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getImportedMessage().replace("%count%", added+"").replace("%skipped%", skipped+""));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getImportErrorMessage().replace("%error%", error));
                        }
                    });
                }
            } else {
                if (format.equals("json")) {
                    plugin.getImportExportManager().importWhitelistJSON(file, merge, (success, added, skipped, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getImportedMessage().replace("%count%", added+"").replace("%skipped%", skipped+""));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getImportErrorMessage().replace("%error%", error));
                        }
                    });
                } else {
                    plugin.getImportExportManager().importWhitelistTXT(file, merge, (success, added, skipped, error) -> {
                        if (success) {
                            sender.sendMessage(plugin.getConfigManager().getImportedMessage().replace("%count%", added+"").replace("%skipped%", skipped+""));
                        } else {
                            sender.sendMessage(plugin.getConfigManager().getImportErrorMessage().replace("%error%", error));
                        }
                    });
                }
            }
            return true;
        }

        // -----------------------------------------------
        if (sub.equals("check")) {
            if (sender.hasPermission("moderation.check")) {
                if (args.length != 3) {
                    if(args.length == 2) {
                        sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                        return true;
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }else{
                    String type = args[1].toLowerCase();
                    String word = checkWord(args[2], sender);
                    if(word == null) return true;
                    if(type.equals("blacklist")){
                        boolean isBlocked = DbManager.isBlacklisted(word);
                        if (isBlocked) {
                            sender.sendMessage(plugin.getConfigManager().getBlacklistedMessage());
                            return true;
                        }else{
                            sender.sendMessage(plugin.getConfigManager().getNotBlacklistedMessage());
                            return true;
                        }
                    }else if(type.equals("whitelist")){
                        boolean isWhitelisted = DbManager.isWhitelisted(word);
                        if(isWhitelisted){
                            sender.sendMessage(plugin.getConfigManager().getWhitelistedMessage());
                            return true;
                        }else{
                            sender.sendMessage(plugin.getConfigManager().getNotWhitelistedMessage());
                            return true;
                        }
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }
            } else {
                sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                return true;
            }
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
                if (args.length != 3) {
                    if(args.length == 2) {
                        sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                        return true;
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }else{
                    String type = args[1];
                    String addWord = checkWord(args[2], sender);
                    if(addWord == null) return true;
                    if(type.equals("whitelist")){
                        DbManager.addWordToWhitelist(addWord, plugin, () ->
                                sender.sendMessage(plugin.getConfigManager().getAddWordToWhitelistMessage())
                        );
                        return true;
                    }else if(type.equals("blacklist")){
                        DbManager.addWordToBlacklist(addWord, plugin, () ->
                                sender.sendMessage(plugin.getConfigManager().getAddWordToBlacklistMessage())
                        );
                        return true;
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }

            case "remove":
                if (!sender.hasPermission("moderation.remove")) {
                    sender.sendMessage(plugin.getConfigManager().getNoPermissionMessage());
                    return true;
                }
                if (args.length != 3) {
                    if(args.length == 2) {
                        sender.sendMessage(plugin.getConfigManager().getNoWordMessage());
                        return true;
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }else{
                    String type = args[1];
                    String addWord = checkWord(args[2], sender);
                    if(addWord == null) return true;
                    if(type.equals("whitelist")){
                        DbManager.removeWordFromWhitelist(addWord, plugin, () ->
                                sender.sendMessage(plugin.getConfigManager().getRemoveWordFromWhitelistMessage())
                        );
                        return true;
                    }else if(type.equals("blacklist")){
                        DbManager.removeWordFromBlacklist(addWord, plugin, () ->
                                sender.sendMessage(plugin.getConfigManager().getRemoveWordFromBlacklistMessage())
                        );
                        return true;
                    }else{
                        sender.sendMessage(plugin.getConfigManager().getUsageMessage());
                        return true;
                    }
                }

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
                for (String line : plugin.getConfigManager().getHelpMessage()) {
                    sender.sendMessage(line);
                }
                break;
        }

        return true;
    }

    private String checkWord(String word, CommandSender sender) {
        String addWord = word.toLowerCase().trim();
        if (addWord.isEmpty()) {
            sender.sendMessage(plugin.getConfigManager().getNoEmptyWordsMessage());
            return null;
        }

        if (addWord.length() > 50) {
            sender.sendMessage(plugin.getConfigManager().getWordTooLongMessage().replace("%max%", plugin.getConfigManager().getMaxWordCharacter()+""));
            return null;
        }

        if (!addWord.matches("[a-z0-9]+")) {
            sender.sendMessage(plugin.getConfigManager().getOnlyLettersNumbersMessage());
            return null;
        }
        return addWord;
    }


    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String @NotNull [] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = List.of("reload", "add", "remove", "check", "history", "export", "import");
            List<String> allowed = new ArrayList<>();

            for (String cmd : commands) {
                switch (cmd) {
                    case "reload" -> {
                        if (sender.hasPermission("moderation.reload")) allowed.add(cmd);
                    }
                    case "add" -> {
                        if (sender.hasPermission("moderation.add")) allowed.add(cmd);
                    }
                    case "remove" -> {
                        if (sender.hasPermission("moderation.remove")) allowed.add(cmd);
                    }
                    case "history" -> {
                        if (sender.hasPermission("moderation.history")) allowed.add(cmd);
                    }
                    case "check" -> {
                        if (sender.hasPermission("moderation.check")) allowed.add(cmd);
                    }
                    case "export" -> {
                        if (sender.hasPermission("moderation.export")) allowed.add(cmd);
                    }
                    case "import" -> {
                        if (sender.hasPermission("moderation.import")) allowed.add(cmd);
                    }
                }
            }

            org.bukkit.util.StringUtil.copyPartialMatches(args[0], allowed, completions);
            completions.sort(String::compareToIgnoreCase);
            return completions;
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("add") && sender.hasPermission("moderation.add")) {
                List<String> types = List.of("blacklist", "whitelist");
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], types, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("remove") && sender.hasPermission("moderation.remove")) {
                List<String> types = List.of("blacklist", "whitelist");
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], types, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("check") && sender.hasPermission("moderation.check")) {
                List<String> types = List.of("blacklist", "whitelist");
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], types, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("export") && sender.hasPermission("moderation.export")) {
                List<String> types = List.of("blacklist", "whitelist");
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], types, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("import") && sender.hasPermission("moderation.import")) {
                List<String> types = List.of("blacklist", "whitelist");
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], types, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("history") && sender.hasPermission("moderation.history")) {
                List<String> playerNames = Bukkit.getOnlinePlayers().stream()
                        .map(org.bukkit.entity.Player::getName)
                        .toList();
                org.bukkit.util.StringUtil.copyPartialMatches(args[1], playerNames, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }
        }

        if (args.length == 3) {
            String subCommand = args[0].toLowerCase();

            if ((subCommand.equals("add") || subCommand.equals("remove") || subCommand.equals("check"))) {
                return List.of();
            }

            if ((subCommand.equals("export") || subCommand.equals("import")) &&
                    (sender.hasPermission("moderation.export") || sender.hasPermission("moderation.import"))) {
                List<String> formats = List.of("json", "txt");
                org.bukkit.util.StringUtil.copyPartialMatches(args[2], formats, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }

            if (subCommand.equals("history") && sender.hasPermission("moderation.history")) {
                return List.of("5", "10", "20", "50", "100");
            }
        }

        if (args.length == 4) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("import") && sender.hasPermission("moderation.import")) {
                File importsFolder = new File(plugin.getDataFolder(), "imports");
                if (importsFolder.exists() && importsFolder.isDirectory()) {
                    File[] files = importsFolder.listFiles();
                    if (files != null) {
                        List<String> fileNames = new ArrayList<>();
                        for (File file : files) {
                            if (file.isFile()) {
                                fileNames.add(file.getName());
                            }
                        }
                        org.bukkit.util.StringUtil.copyPartialMatches(args[3], fileNames, completions);
                        completions.sort(String::compareToIgnoreCase);
                        return completions;
                    }
                }
            }
        }

        if (args.length == 5) {
            String subCommand = args[0].toLowerCase();

            if (subCommand.equals("import") && sender.hasPermission("moderation.import")) {
                List<String> modes = List.of("merge", "replace");
                org.bukkit.util.StringUtil.copyPartialMatches(args[4], modes, completions);
                completions.sort(String::compareToIgnoreCase);
                return completions;
            }
        }

        return List.of();
    }

}