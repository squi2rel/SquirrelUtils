package com.github.squi2rel.mcutils;

import arc.Events;
import com.github.squi2rel.mcutils.events.Trigger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import static com.github.squi2rel.mcutils.Vars.plugin;

public class MyPlugin extends JavaPlugin {
    public static void init() {
        Vars.init();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Events.fire(Trigger.load);
        saveDefaultConfig();
    }

    @Override
    public void onEnable() {
        plugin = this;
        getLogger().info("onEnable is called!");
        Vars.config = getConfig();
        init();
        Events.fire(Trigger.enable);
    }
    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
        Events.fire(Trigger.disable);
        saveConfig();
    }

    @Override
    public PluginCommand getCommand(@NotNull String name) {
        return super.getCommand(name);
    }
}