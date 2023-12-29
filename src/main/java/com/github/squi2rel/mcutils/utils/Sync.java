package com.github.squi2rel.mcutils.utils;

import com.github.squi2rel.mcutils.Vars;
import org.bukkit.Bukkit;

import java.util.ArrayList;

public class Sync {
    private static final ArrayList<Runnable> list = new ArrayList<>();
    public static void init() {
        Bukkit.getScheduler().runTaskTimer(Vars.plugin, Sync::runTask, 0, 1);
    }

    public static void post(Runnable r) {
        list.add(r);
    }

    private static void runTask() {
        for (Runnable runnable : list) runnable.run();
        list.clear();
    }
}
