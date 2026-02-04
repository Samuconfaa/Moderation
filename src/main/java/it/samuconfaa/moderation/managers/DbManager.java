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

    private static HikariDataSource dataSource;
    private static final Set<String> BLACKLIST = ConcurrentHashMap.newKeySet();


    public static void init(Moderation plugin) {
        File dataFolder = plugin.getDataFolder();
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
            dataSource.close();
        }
    }

    private static void createTables(Moderation plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sqlWords = """
                CREATE TABLE IF NOT EXISTS blacklist_words (
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
                Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                    loadBlacklist(plugin);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public static void isBlacklisted(String word, Moderation plugin, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            boolean found = false;
            String sql = "SELECT 1 FROM blacklist_words WHERE word = ? LIMIT 1";

            try (var conn = DbManager.getConnection();
                 var ps = conn.prepareStatement(sql)) {

                ps.setString(1, word.toLowerCase());
                var rs = ps.executeQuery();
                found = rs.next();

            } catch (Exception e) {
                e.printStackTrace();
            }

            boolean finalFound = found;
            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(finalFound));
        });
    }

    /*
        DbManager.isBlacklisted("cazzo", plugin, isBlocked -> {
            if (isBlocked) {
                //parola vietata
            }
        });
     */

    public static void loadBlacklistAsync(Moderation plugin) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String sql = "SELECT word FROM blacklist_words";

            try (var conn = DbManager.getConnection();
                 var stmt = conn.createStatement();
                 var rs = stmt.executeQuery(sql)) {

                BLACKLIST.clear();
                while (rs.next()) {
                    BLACKLIST.add(rs.getString("word"));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
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

    public static void containsBlacklistedWordCachedAsync(String message, Moderation plugin, Consumer<String> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String msg = message.toLowerCase();
            String foundWord = BLACKLIST.stream()
                    .filter(msg::contains)
                    .findFirst()
                    .orElse(null);

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(foundWord));
        });
    }

    public static String containsBlacklistedWordCached(String message, Moderation plugin) {
        String msg = message.toLowerCase();

        for (String word : BLACKLIST) {
            if (msg.contains(word)) return word;
        }
        return null;

    }

    /*
        DbManager.containsBlacklistedWordCached(message, plugin, found -> {
            if (found) {
                //Se ha trovato
            }
        });
     */

    public static void addWord(String word, Moderation plugin, Runnable callback) {
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

    public static void removeWord(String word, Moderation plugin, Runnable callback) {
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
