package it.samuconfaa.moderation.managers;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import it.samuconfaa.moderation.Moderation;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class DbManager {

    private static HikariDataSource dataSource;
    private static final Set<String> BLACKLIST = ConcurrentHashMap.newKeySet();
    ;

    public static void init(Moderation plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists()) dataFolder.mkdirs();

        File dbFile = new File(dataFolder, plugin.getConfigManager().getDbName());

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:sqlite:" + dbFile.getAbsolutePath());
        config.setDriverClassName("org.sqlite.JDBC");

        config.setMaximumPoolSize(1);
        config.setConnectionTestQuery("SELECT 1");
        config.setPoolName("ModerationPool");

        dataSource = new HikariDataSource(config);

        createTables(plugin);
        loadBlacklist(plugin);
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
            String sql = """
                CREATE TABLE IF NOT EXISTS blacklist_words (
                    word TEXT PRIMARY KEY
                );
            """;

            try (var conn = getConnection();
                 var stmt = conn.createStatement()) {

                stmt.execute(sql);

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

    public static void loadBlacklist(Moderation plugin) {
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

    public static void containsBlacklistedWordCached(String message, Moderation plugin, Consumer<Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String msg = message.toLowerCase();
            boolean found = BLACKLIST.stream().anyMatch(msg::contains);

            Bukkit.getScheduler().runTask(plugin, () -> callback.accept(found));
        });
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



}
