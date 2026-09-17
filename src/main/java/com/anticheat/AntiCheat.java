package com.anticheat;

import com.anticheat.listeners.PacketListener;
import com.anticheat.listeners.PlayerConnectionListener;
import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiCheat extends JavaPlugin implements CommandExecutor {

    private static AntiCheat instance;
    private final Map<UUID, PlayerData> playerDataMap = new ConcurrentHashMap<>();

    @Override
    public void onLoad() {
        instance = this;
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().getSettings().checkForUpdates(false).bStats(false);
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener());
        
        getServer().getPluginManager().registerEvents(new PlayerConnectionListener(), this);
        if (getCommand("vulcanlite") != null) {
            getCommand("vulcanlite").setExecutor(this);
        }

        getLogger().info("========================================");
        getLogger().info(" VulcanLite AntiCheat (Purpur 1.21.1) ");
        getLogger().info(" Inizializzato con successo! ");
        getLogger().info("========================================");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        playerDataMap.clear();
    }

    public static AntiCheat getInstance() {
        return instance;
    }

    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, PlayerData::new);
    }

    public void removePlayerData(UUID uuid) {
        playerDataMap.remove(uuid);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("vulcanlite.admin")) {
            sender.sendMessage("§cNon hai i permessi necessari per usare questo comando.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("alerts")) {
            if (sender instanceof Player player) {
                PlayerData data = getPlayerData(player.getUniqueId());
                data.setAlertsEnabled(!data.isAlertsEnabled());
                player.sendMessage("§e[VulcanLite] §fAvvisi " + (data.isAlertsEnabled() ? "§aattivati" : "§cdisattivati"));
            } else {
                sender.sendMessage("Questo comando può essere eseguito solo in gioco.");
            }
            return true;
        }

        sender.sendMessage("§e---- §c§lVulcanLite AntiCheat §e----");
        sender.sendMessage("§f/vl alerts §7- Attiva/Disattiva notifiche alert");
        return true;
    }
}