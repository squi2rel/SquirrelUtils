package com.github.squi2rel.mcutils;

import com.github.squi2rel.mcutils.fun.ImageToMap;
import com.github.squi2rel.mcutils.fun.JB;
import com.github.squi2rel.mcutils.useful.DigLeader;
import com.github.squi2rel.mcutils.useful.JS;
import com.github.squi2rel.mcutils.useful.PearlLoadChunks;
import com.github.squi2rel.mcutils.utils.Sync;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;

import java.util.Objects;

public class Vars {
    public static MyPlugin plugin;

    public static Economy economy;
    public static void init() {
        new ImageToMap();
        new JB();
        new JS();
        new DigLeader();
        new PearlLoadChunks();

        Sync.init();

        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") != null) economy = Objects.requireNonNull(Bukkit.getServer().getServicesManager().getRegistration(Economy.class)).getProvider();
    }
}
