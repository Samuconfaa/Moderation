package it.samuconfaa.moderation.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class LicenseManager {

    private final String pluginName;
    private final String licenseKey;
    private final String apiUrl = "https://samuconfa.it/attivazioni/check";
    private final JavaPlugin plugin;

    public LicenseManager(JavaPlugin plugin, String pluginName, String licenseKey) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.licenseKey = licenseKey;
    }

    public void check() {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String fullUrl = apiUrl + "?plugin=" + pluginName + "&key=" + licenseKey;
                HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.addRequestProperty("User-Agent", "Mozilla/5.0");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

                    if (!response.has("status")) {
                        stopPlugin("Risposta del server non valida.");
                        return;
                    }

                    String status = response.get("status").getAsString();

                    if (status.equals("success")) {
                        plugin.getLogger().info("§aLicenza verificata con successo!");

                        if (response.has("latest_version")) {
                            String latestVersion = response.get("latest_version").getAsString();
                            String currentVersion = plugin.getDescription().getVersion();

                            if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                    Bukkit.getConsoleSender().sendMessage("");
                                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------");
                                    Bukkit.getConsoleSender().sendMessage("§6§l[ATTENZIONE] §fPlugin: §b" + plugin.getName());
                                    Bukkit.getConsoleSender().sendMessage("§eÈ disponibile una nuova versione: §a" + latestVersion);
                                    Bukkit.getConsoleSender().sendMessage("§7Stai attualmente utilizzando la §c" + currentVersion);
                                    Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------");
                                    Bukkit.getConsoleSender().sendMessage("");
                                }, 400L);
                            }
                        }
                    } else {
                        String message = response.has("message") ? response.get("message").getAsString() : "Licenza non valida o scaduta.";
                        stopPlugin(message);
                    }
                } else {
                    stopPlugin("Impossibile connettersi al server (Errore HTTP: " + conn.getResponseCode() + ")");
                }

            } catch (Exception e) {
                stopPlugin("Errore critico durante la verifica: " + e.getMessage());
            }
        });
    }

    private void stopPlugin(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe("§c[LICENZA] " + reason);
            plugin.getLogger().severe("§cIl plugin verrà disabilitato.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
    }
}