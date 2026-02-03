package it.samuconfaa.moderation.listeners;

import it.samuconfaa.moderation.Moderation;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;

public class SignChangeListener implements Listener {
    private Moderation plugin;
    public SignChangeListener(Moderation plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSignChange(SignChangeEvent event) {

    }
}
