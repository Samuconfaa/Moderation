package it.samuconfaa.moderation.managers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import it.samuconfaa.moderation.Moderation;
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
    private final Moderation plugin;

    public LicenseManager(Moderation plugin, String pluginName, String licenseKey) {
        this.plugin = plugin;
        this.pluginName = pluginName;
        this.licenseKey = licenseKey;
    }

    public void check() {
        try {
            String fullUrl = apiUrl + "?plugin=" + pluginName.replace(" ", "%20") + "&key=" + licenseKey;
            HttpURLConnection conn = (HttpURLConnection) new URL(fullUrl).openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Mozilla/5.0");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                JsonObject response = JsonParser.parseReader(reader).getAsJsonObject();

                if (!response.has("status")) {
                    stopPlugin("Invalid server response.");
                    return;
                }

                String status = response.get("status").getAsString();

                if (status.equals("success")) {
                    plugin.getLogger().info("§aLicense verified successfully!");

                    if (response.has("latest_version")) {
                        String latestVersion = response.get("latest_version").getAsString();
                        String currentVersion = plugin.getDescription().getVersion();

                        if (!latestVersion.equalsIgnoreCase(currentVersion)) {
                            plugin.setUpdate(true);
                            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                                Bukkit.getConsoleSender().sendMessage("");
                                Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------");
                                Bukkit.getConsoleSender().sendMessage("§6§l[UPDATE] §fPlugin: §b" + plugin.getName());
                                Bukkit.getConsoleSender().sendMessage("§eA new version is available: §a" + latestVersion);
                                Bukkit.getConsoleSender().sendMessage("§7You are currently running: §c" + currentVersion);
                                Bukkit.getConsoleSender().sendMessage("§8§m----------------------------------");
                                Bukkit.getConsoleSender().sendMessage("");
                            }, 400L);
                        }
                    }
                } else {
                    String message = response.has("message") ? response.get("message").getAsString() : "Invalid or expired license.";
                    stopPlugin(message);
                }
            } else {
                stopPlugin("Connection to license server failed (HTTP Error: " + conn.getResponseCode() + ")");
            }

        } catch (Exception e) {
            stopPlugin("Critical error during verification: " + e.getMessage());
        }
    }

    private void stopPlugin(String reason) {
        Bukkit.getScheduler().runTask(plugin, () -> {
            plugin.getLogger().severe("§c[LICENSE] " + reason);
            plugin.getLogger().severe("§cThe plugin will be disabled.");
            Bukkit.getPluginManager().disablePlugin(plugin);
        });
    }
}