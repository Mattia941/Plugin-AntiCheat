package com.anticheat;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    
    // Position & Movement Tracking
    private double lastX, lastY, lastZ;
    private double deltaX, deltaY, deltaZ;
    private double deltaXZ;
    private boolean onGround;
    private boolean lastOnGround;
    private int airTicks;
    private int groundTicks;

    // Combat Tracking
    private long lastSwingTime;
    private long lastAttackTime;
    private double lastYaw, lastPitch;
    private double deltaYaw, deltaPitch;

    // Violation Levels (VL)
    private int flyVl = 0;
    private int speedVl = 0;
    private int killauraVl = 0;
    private int autoclickerVl = 0;

    private boolean alertsEnabled = true;

    public PlayerData(UUID uuid) {
        this.uuid = uuid;
    }

    public void updatePosition(double x, double y, double z, boolean clientOnGround) {
        this.deltaX = x - this.lastX;
        this.deltaY = y - this.lastY;
        this.deltaZ = z - this.lastZ;
        this.deltaXZ = Math.hypot(deltaX, deltaZ);

        this.lastX = x;
        this.lastY = y;
        this.lastZ = z;

        this.lastOnGround = this.onGround;
        this.onGround = clientOnGround;

        if (onGround) {
            groundTicks++;
            airTicks = 0;
        } else {
            airTicks++;
            groundTicks = 0;
        }
    }

    public void updateRotation(float yaw, float pitch) {
        this.deltaYaw = Math.abs(yaw - this.lastYaw);
        this.deltaPitch = Math.abs(pitch - this.lastPitch);
        this.lastYaw = yaw;
        this.lastPitch = pitch;
    }

    // Getters & Setters
    public UUID getUuid() { return uuid; }
    public double getDeltaY() { return deltaY; }
    public double getDeltaXZ() { return deltaXZ; }
    public boolean isOnGround() { return onGround; }
    public int getAirTicks() { return airTicks; }
    
    public long getLastSwingTime() { return lastSwingTime; }
    public void setLastSwingTime(long lastSwingTime) { this.lastSwingTime = lastSwingTime; }

    public double getDeltaYaw() { return deltaYaw; }
    public double getDeltaPitch() { return deltaPitch; }

    public int getFlyVl() { return flyVl; }
    public void addFlyVl(int amount) { this.flyVl += amount; }
    
    public int getSpeedVl() { return speedVl; }
    public void addSpeedVl(int amount) { this.speedVl += amount; }

    public int getKillauraVl() { return killauraVl; }
    public void addKillauraVl(int amount) { this.killauraVl += amount; }

    public int getAutoclickerVl() { return autoclickerVl; }
    public void addAutoclickerVl(int amount) { this.autoclickerVl += amount; }

    public boolean isAlertsEnabled() { return alertsEnabled; }
    public void setAlertsEnabled(boolean alertsEnabled) { this.alertsEnabled = alertsEnabled; }
}