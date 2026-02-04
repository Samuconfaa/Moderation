package it.samuconfaa.moderation.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.samuconfaa.moderation.Moderation;
import it.samuconfaa.moderation.models.DbSegnalationModel;
import it.samuconfaa.moderation.models.EnumAction;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DbManager {
    private static Moderation pluginInstance;
    private static HikariDataSource dataSource;
    private static final Set<String> BLACKLIST = ConcurrentHashMap.newKeySet();
    private static final Set<String> WHITELIST = ConcurrentHashMap.newKeySet();

    //========= METODI DB INIZIALI ============

    public static void init(Moderation plugin) {
        pluginInstance = plugin;
        File dataFolder = pluginInstance.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File dbFile = new File(dataFolder, plugin.getConfigManager().getDbName());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(3);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("ModerationPool");

        dataSource = new HikariDataSource(config);

        createTables(plugin);
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            try {
                dataSource.setMaximumPoolSize(0);
                Thread.sleep(1000);
                dataSource.close();
                pluginInstance.getLogger().info("Database connection closed successfully");
            } catch (Exception e) {
                pluginInstance.getLogger().severe("Error closing database: " + e.getMessage());
            }
        }
    }

    private static void createTables(Moderation plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sqlWords = """
                CREATE TABLE IF NOT EXISTS blacklist_words (
                    word TEXT PRIMARY KEY
                );
            """;

            String sqlWhitelist = """
                CREATE TABLE IF NOT EXISTS whitelist_words (
                    word TEXT PRIMARY KEY
                );
            """;

            String sqlHistory = """
                CREATE TABLE IF NOT EXISTS history (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    playerUUID TEXT NOT NULL,
                    message TEXT NOT NULL,
                    data_ora TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    enumAction TEXT NOT NULL
                );
            """;

            try (var conn = getConnection();
                 var stmt = conn.createStatement()) {

                stmt.execute(sqlWords);
                stmt.execute(sqlHistory);
                stmt.execute(sqlWhitelist);

                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    loadBlacklist(plugin);
                    loadWhitelist(plugin);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private static void loadWhitelist(Moderation plugin) {
        String sql = "SELECT word FROM whitelist_words";
        try (var conn = getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            WHITELIST.clear();
            while (rs.next()) {
                WHITELIST.add(rs.getString("word"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadBlacklist(Moderation plugin) {
        String sql = "SELECT word FROM blacklist_words";
        try (var conn = getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery(sql)) {

            BLACKLIST.clear();
            while (rs.next()) {
                BLACKLIST.add(rs.getString("word"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //========= METODI DB WHITELIST ============

    public static boolean isWhitelisted(String word) {
        return WHITELIST.contains(word);
    }

    public static void addWordToWhitelist(String word, Moderation plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT OR IGNORE INTO whitelist_words (word) VALUES (?)";

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, word.toLowerCase());
                ps.executeUpdate();

                WHITELIST.add(word.toLowerCase());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    public static void removeWordFromWhitelist(String word, Moderation plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "DELETE FROM whitelist_words WHERE word = ?";

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, word.toLowerCase());
                ps.executeUpdate();

                WHITELIST.remove(word.toLowerCase());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    public static String containsWhitelistedWord(String message, Moderation plugin) {
        String msg = message.toLowerCase();

        for (String word : BLACKLIST) {
            if (msg.contains(word)) return word;
        }
        return null;
    }

    //========= METODI DB BLACKLIST ============

    public static boolean isBlacklisted(String word) {
        return BLACKLIST.contains(word);
    }

    public static String containsBlacklistedWord(String message) {
        String normalized = message.toLowerCase();

        String[] words = normalized.split("\\s+");

        for (String word : words) {
            if (WHITELIST.contains(word)) {
                continue;
            }

            if (BLACKLIST.contains(word)) {
                return word;
            }

            for (String blacklisted : BLACKLIST) {
                if (word.contains(blacklisted)) {
                    return blacklisted;
                }
            }
        }

        String noSpaces = normalized.replaceAll("\\s+", "");
        for (String blacklisted : BLACKLIST) {
            if (noSpaces.contains(blacklisted)) {
                boolean isPartOfWhitelisted = false;
                for (String whitelisted : WHITELIST) {
                    if (noSpaces.contains(whitelisted) && whitelisted.contains(blacklisted)) {
                        isPartOfWhitelisted = true;
                        break;
                    }
                }
                if (!isPartOfWhitelisted) {
                    return blacklisted;
                }
            }
        }

        return null;
    }

    public static void addWordToBlacklist(String word, Moderation plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "INSERT OR IGNORE INTO blacklist_words (word) VALUES (?)";

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, word.toLowerCase());
                ps.executeUpdate();

                BLACKLIST.add(word.toLowerCase());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    public static void removeWordFromBlacklist(String word, Moderation plugin, Runnable callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "DELETE FROM blacklist_words WHERE word = ?";

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, word.toLowerCase());
                ps.executeUpdate();

                BLACKLIST.remove(word.toLowerCase());

            } catch (Exception e) {
                e.printStackTrace();
            }

            if (callback != null) {
                Bukkit.getScheduler().runTask(plugin, callback);
            }
        });
    }

    /*
        DbManager.addWord("cazzo", plugin, () -> {
            //
        });

        DbManager.removeWord("cazzo", plugin, () -> {
            //
        });
     */

    //===============METODI HISTORY==========================
    public static void addHistory(Moderation plugin, String playerUUID, String message, EnumAction enumAction) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->{
            String sql = """
                INSERT INTO history (playerUUID, message, enumAction)
                VALUES (?, ?, ?)
            """;

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, playerUUID);
                ps.setString(2, message);
                ps.setString(3, enumAction.name());
                ps.executeUpdate();

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void getHistory(Moderation plugin, String playerUUID, int limit, Consumer<List<DbSegnalationModel>> callback) {
        if(limit == 0){
            limit = plugin.getConfigManager().getHistoryLimit();
        }
        int finalLimit = limit;
        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->{
            List<DbSegnalationModel> history = new ArrayList<>();

            String sql = """
                SELECT id, playerUUID, message, data_ora, enumAction
                FROM history
                WHERE playerUUID = ?
                ORDER BY data_ora DESC
                LIMIT ?
            """;
            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, playerUUID);
                ps.setInt(2, finalLimit);

                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {

                        EnumAction enumAction;
                        try {
                            enumAction = EnumAction.valueOf(
                                    rs.getString("enumAction")
                            );
                        } catch (Exception e) {
                            enumAction = EnumAction.PLUGIN_ERROR;
                        }

                        DbSegnalationModel model = new DbSegnalationModel(
                                rs.getInt("id"),
                                UUID.fromString(rs.getString("playerUUID")),
                                rs.getString("message"),
                                rs.getTimestamp("data_ora"),
                                enumAction
                        );

                        history.add(model);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(history));
        });
    }

}
