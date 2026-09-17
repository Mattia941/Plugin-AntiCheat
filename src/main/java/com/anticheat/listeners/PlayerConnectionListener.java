package com.anticheat.listeners;

import com.anticheat.AntiCheat;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerConnectionListener implements Listener {

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        AntiCheat.getInstance().removePlayerData(event.getPlayer().getUniqueId());
    }
}