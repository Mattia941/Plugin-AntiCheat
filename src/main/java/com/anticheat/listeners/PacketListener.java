package com.anticheat.listeners;

import com.anticheat.AntiCheat;
import com.anticheat.PlayerData;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.play.in.WrapperPlayClientPlayerFlying;
import com.github.retrooper.packetevents.wrapper.play.in.WrapperPlayClientAnimation;
import com.github.retrooper.packetevents.wrapper.play.in.WrapperPlayClientInteractEntity;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PacketListener extends PacketListenerAbstract {

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        Player player = (Player) event.getPlayer();
        if (player == null) return;

        PlayerData data = AntiCheat.getInstance().getPlayerData(player.getUniqueId());

        // 1. Movement Checks (Fly & Speed)
        if (PacketType.Play.Client.isInstanceOfFlying(event.getPacketType())) {
            WrapperPlayClientPlayerFlying packet = new WrapperPlayClientPlayerFlying(event);

            if (packet.hasPositionChanged()) {
                double x = packet.getLocation().getX();
                double y = packet.getLocation().getY();
                double z = packet.getLocation().getZ();
                boolean onGround = packet.isOnGround();

                data.updatePosition(x, y, z, onGround);

                // --- Fly (Type A): Inconsistenza Y su terreno falso ---
                if (onGround && data.getDeltaY() > 0.0 && y % 0.015625 != 0) {
                    data.addFlyVl(1);
                    alert(player, "Fly", "Type A", data.getFlyVl());
                }

                // --- Speed (Type A): Velocità orizzontale anomala ---
                if (data.getDeltaXZ() > 0.66 && !player.isFlying() && !player.isGliding()) {
                    data.addSpeedVl(1);
                    alert(player, "Speed", "Type A (OverSpeed: " + String.format("%.2f", data.getDeltaXZ()) + ")", data.getSpeedVl());
                }
            }

            if (packet.hasRotationChanged()) {
                data.updateRotation(packet.getLocation().getYaw(), packet.getLocation().getPitch());
            }
        }

        // 2. AutoClicker Check (Anomalous CPS / Delta Timing)
        if (event.getPacketType() == PacketType.Play.Client.ANIMATION) {
            WrapperPlayClientAnimation packet = new WrapperPlayClientAnimation(event);
            long now = System.currentTimeMillis();
            long lastSwing = data.getLastSwingTime();
            data.setLastSwingTime(now);

            long diff = now - lastSwing;
            if (diff > 0 && diff < 15) { // Meno di 15ms tra due swing -> CPS > 60 o Macro
                data.addAutoclickerVl(1);
                alert(player, "AutoClicker", "Type A (Delay: " + diff + "ms)", data.getAutoclickerVl());
            }
        }

        // 3. Killaura Check (Rotazioni / Hit Angle)
        if (event.getPacketType() == PacketType.Play.Client.INTERACT_ENTITY) {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() == WrapperPlayClientInteractEntity.InteractAction.ATTACK) {
                // Se la rotazione immediata durante l'attacco è innaturale (> 120° in un singolo tick)
                if (data.getDeltaYaw() > 120.0f) {
                    data.addKillauraVl(1);
                    alert(player, "KillAura", "Type A (Snap Yaw: " + String.format("%.1f", data.getDeltaYaw()) + "°)", data.getKillauraVl());
                }
            }
        }
    }

    private void alert(Player player, String check, String type, int vl) {
        String msg = "§c[VulcanLite] §f" + player.getName() + " §7ha fallito §c" + check + " §8(" + type + ") §e[VL: " + vl + "]";
        Bukkit.getOnlinePlayers().stream()
                .filter(p -> p.hasPermission("vulcanlite.admin"))
                .forEach(p -> {
                    PlayerData d = AntiCheat.getInstance().getPlayerData(p.getUniqueId());
                    if (d.isAlertsEnabled()) {
                        p.sendMessage(msg);
                    }
                });
    }
}