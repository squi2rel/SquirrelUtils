package com.github.squi2rel.mcutils.utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public class Position extends Location {

    public Position(@Nullable World world, double x, double y, double z) {
        super(world, x, y, z);
    }

    public Position(@Nullable World world, double x, double y, double z, float yaw, float pitch) {
        super(world, x, y, z, yaw, pitch);
    }

    public Position(Location loc) {
        this(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
    }

    public Position copy() {
        return new Position(this);
    }

    public Position x(double x) {
        setX(x);
        return this;
    }

    public Position y(double y) {
        setY(y);
        return this;
    }

    public Position z(double z) {
        setZ(z);
        return this;
    }

    public Position pitch(float pitch) {
        setPitch(pitch);
        return this;
    }

    public Position yaw(float yaw) {
        setYaw(yaw);
        return this;
    }

    public Position set(Location loc) {
        setX(loc.getX());
        setY(loc.getY());
        setZ(loc.getZ());
        setPitch(loc.getPitch());
        setYaw(loc.getYaw());
        return this;
    }

    @Override
    public @NotNull Position add(double x, double y, double z) {
        super.add(x, y, z);
        return this;
    }

    public Position addX(double x) {
        add(x, 0, 0);
        return this;
    }

    public Position addY(double y) {
        add(0, y, 0);
        return this;
    }

    public Position addZ(double z) {
        add(0, 0, z);
        return this;
    }

    public static Position from(Location loc) {
        return new Position(loc);
    }
}
