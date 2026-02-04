package it.samuconfaa.moderation.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import it.samuconfaa.moderation.Moderation;
import org.bukkit.Bukkit;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ImportExportManager {

    private final Moderation plugin;
    private final Gson gson;

    public ImportExportManager(Moderation plugin) {
        this.plugin = plugin;
        this.gson = new GsonBuilder().setPrettyPrinting().create();
    }

    // ==================== EXPORT ====================


    public void exportBlacklistJSON(ExportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Set<String> blacklist = getBlacklistFromDB();
                File file = new File(plugin.getDataFolder(), "exports/blacklist.json");
                file.getParentFile().mkdirs();

                JsonObject json = new JsonObject();
                json.addProperty("type", "blacklist");
                json.addProperty("exportDate", System.currentTimeMillis());
                json.addProperty("count", blacklist.size());

                JsonArray wordsArray = new JsonArray();
                blacklist.forEach(wordsArray::add);
                json.add("words", wordsArray);

                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(json, writer);
                }

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, file, blacklist.size(), null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, null, 0, e.getMessage())
                );
            }
        });
    }

    public void exportWhitelistJSON(ExportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Set<String> whitelist = getWhitelistFromDB();
                File file = new File(plugin.getDataFolder(), "exports/whitelist.json");
                file.getParentFile().mkdirs();

                JsonObject json = new JsonObject();
                json.addProperty("type", "whitelist");
                json.addProperty("exportDate", System.currentTimeMillis());
                json.addProperty("count", whitelist.size());

                JsonArray wordsArray = new JsonArray();
                whitelist.forEach(wordsArray::add);
                json.add("words", wordsArray);

                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(json, writer);
                }

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, file, whitelist.size(), null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, null, 0, e.getMessage())
                );
            }
        });
    }

    public void exportBlacklistTXT(ExportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Set<String> blacklist = getBlacklistFromDB();
                File file = new File(plugin.getDataFolder(), "exports/blacklist.txt");
                file.getParentFile().mkdirs();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("# Blacklist exported on: " + new java.util.Date());
                    writer.newLine();
                    writer.write("# Total words: " + blacklist.size());
                    writer.newLine();
                    writer.newLine();

                    for (String word : blacklist) {
                        writer.write(word);
                        writer.newLine();
                    }
                }

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, file, blacklist.size(), null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, null, 0, e.getMessage())
                );
            }
        });
    }


    public void exportWhitelistTXT(ExportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                Set<String> whitelist = getWhitelistFromDB();
                File file = new File(plugin.getDataFolder(), "exports/whitelist.txt");
                file.getParentFile().mkdirs();

                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                    writer.write("# Whitelist exported on: " + new java.util.Date());
                    writer.newLine();
                    writer.write("# Total words: " + whitelist.size());
                    writer.newLine();
                    writer.newLine();

                    for (String word : whitelist) {
                        writer.write(word);
                        writer.newLine();
                    }
                }

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, file, whitelist.size(), null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, null, 0, e.getMessage())
                );
            }
        });
    }

    // ==================== IMPORT ====================

    /**
     * Importa blacklist da file JSON
     * @param file File da importare
     * @param merge Se true, unisce con lista esistente. Se false, sostituisce.
     * @param callback Callback eseguito al completamento
     */
    public void importBlacklistJSON(File file, boolean merge, ImportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!file.exists()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "File not found!")
                    );
                    return;
                }

                JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);

                if (!json.has("words") || !json.get("words").isJsonArray()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "Invalid JSON format!")
                    );
                    return;
                }

                JsonArray wordsArray = json.getAsJsonArray("words");
                List<String> words = new ArrayList<>();

                wordsArray.forEach(element -> {
                    String word = element.getAsString().toLowerCase().trim();
                    if (!word.isEmpty() && word.matches("[a-z0-9]+") && word.length() <= 50) {
                        words.add(word);
                    }
                });

                if (words.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "No valid words found in file!")
                    );
                    return;
                }

                // Se non merge, svuota la blacklist
                if (!merge) {
                    clearBlacklist();
                }

                // Aggiungi tutte le parole
                int added = 0;
                int skipped = 0;

                for (String word : words) {
                    if (!DbManager.isBlacklisted(word)) {
                        DbManager.addWordToBlacklist(word, plugin, null);
                        added++;
                    } else {
                        skipped++;
                    }
                }

                final int finalAdded = added;
                final int finalSkipped = skipped;

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, finalAdded, finalSkipped, null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, 0, 0, e.getMessage())
                );
            }
        });
    }

    /**
     * Importa whitelist da file JSON
     * @param file File da importare
     * @param merge Se true, unisce con lista esistente. Se false, sostituisce.
     * @param callback Callback eseguito al completamento
     */
    public void importWhitelistJSON(File file, boolean merge, ImportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!file.exists()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "File not found!")
                    );
                    return;
                }

                JsonObject json = gson.fromJson(new FileReader(file), JsonObject.class);

                if (!json.has("words") || !json.get("words").isJsonArray()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "Invalid JSON format!")
                    );
                    return;
                }

                JsonArray wordsArray = json.getAsJsonArray("words");
                List<String> words = new ArrayList<>();

                wordsArray.forEach(element -> {
                    String word = element.getAsString().toLowerCase().trim();
                    if (!word.isEmpty() && word.matches("[a-z0-9]+") && word.length() <= 50) {
                        words.add(word);
                    }
                });

                if (words.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "No valid words found in file!")
                    );
                    return;
                }

                // Se non merge, svuota la whitelist
                if (!merge) {
                    clearWhitelist();
                }

                // Aggiungi tutte le parole
                int added = 0;
                int skipped = 0;

                for (String word : words) {
                    if (!DbManager.isWhitelisted(word)) {
                        DbManager.addWordToWhitelist(word, plugin, null);
                        added++;
                    } else {
                        skipped++;
                    }
                }

                final int finalAdded = added;
                final int finalSkipped = skipped;

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, finalAdded, finalSkipped, null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, 0, 0, e.getMessage())
                );
            }
        });
    }

    /**
     * Importa blacklist da file TXT (una parola per riga)
     * @param file File da importare
     * @param merge Se true, unisce con lista esistente. Se false, sostituisce.
     * @param callback Callback eseguito al completamento
     */
    public void importBlacklistTXT(File file, boolean merge, ImportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!file.exists()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "File not found!")
                    );
                    return;
                }

                List<String> words = Files.readAllLines(file.toPath())
                        .stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .filter(word -> word.matches("[a-z0-9]+") && word.length() <= 50)
                        .collect(Collectors.toList());

                if (words.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "No valid words found in file!")
                    );
                    return;
                }

                // Se non merge, svuota la blacklist
                if (!merge) {
                    clearBlacklist();
                }

                // Aggiungi tutte le parole
                int added = 0;
                int skipped = 0;

                for (String word : words) {
                    if (!DbManager.isBlacklisted(word)) {
                        DbManager.addWordToBlacklist(word, plugin, null);
                        added++;
                    } else {
                        skipped++;
                    }
                }

                final int finalAdded = added;
                final int finalSkipped = skipped;

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, finalAdded, finalSkipped, null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, 0, 0, e.getMessage())
                );
            }
        });
    }

    /**
     * Importa whitelist da file TXT (una parola per riga)
     * @param file File da importare
     * @param merge Se true, unisce con lista esistente. Se false, sostituisce.
     * @param callback Callback eseguito al completamento
     */
    public void importWhitelistTXT(File file, boolean merge, ImportCallback callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                if (!file.exists()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "File not found!")
                    );
                    return;
                }

                List<String> words = Files.readAllLines(file.toPath())
                        .stream()
                        .map(String::trim)
                        .map(String::toLowerCase)
                        .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                        .filter(word -> word.matches("[a-z0-9]+") && word.length() <= 50)
                        .collect(Collectors.toList());

                if (words.isEmpty()) {
                    Bukkit.getScheduler().runTask(plugin, () ->
                            callback.onComplete(false, 0, 0, "No valid words found in file!")
                    );
                    return;
                }

                // Se non merge, svuota la whitelist
                if (!merge) {
                    clearWhitelist();
                }

                // Aggiungi tutte le parole
                int added = 0;
                int skipped = 0;

                for (String word : words) {
                    if (!DbManager.isWhitelisted(word)) {
                        DbManager.addWordToWhitelist(word, plugin, null);
                        added++;
                    } else {
                        skipped++;
                    }
                }

                final int finalAdded = added;
                final int finalSkipped = skipped;

                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(true, finalAdded, finalSkipped, null)
                );

            } catch (Exception e) {
                Bukkit.getScheduler().runTask(plugin, () ->
                        callback.onComplete(false, 0, 0, e.getMessage())
                );
            }
        });
    }

    // ==================== HELPER METHODS ====================

    private Set<String> getBlacklistFromDB() {
        String sql = "SELECT word FROM blacklist_words";
        Set<String> words = new java.util.HashSet<>();

        try (var conn = DbManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                words.add(rs.getString("word"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return words;
    }

    private Set<String> getWhitelistFromDB() {
        String sql = "SELECT word FROM whitelist_words";
        Set<String> words = new java.util.HashSet<>();

        try (var conn = DbManager.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                words.add(rs.getString("word"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return words;
    }

    private void clearBlacklist() {
        String sql = "DELETE FROM blacklist_words";
        try (var conn = DbManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearWhitelist() {
        String sql = "DELETE FROM whitelist_words";
        try (var conn = DbManager.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==================== CALLBACKS ====================

    @FunctionalInterface
    public interface ExportCallback {
        void onComplete(boolean success, File file, int count, String error);
    }

    @FunctionalInterface
    public interface ImportCallback {
        void onComplete(boolean success, int added, int skipped, String error);
    }
}