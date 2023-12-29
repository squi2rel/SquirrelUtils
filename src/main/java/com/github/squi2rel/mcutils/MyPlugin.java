package com.github.squi2rel.mcutils;

import com.github.squi2rel.mcutils.events.Events;
import com.github.squi2rel.mcutils.events.Trigger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import javax.script.ScriptEngineManager;

import static com.github.squi2rel.mcutils.Vars.plugin;

public class MyPlugin extends JavaPlugin {
    public static void init() {
        Vars.init();
        Events.init();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        Events.fire(Trigger.load);
    }

    @Override
    public void onEnable() {
        new ScriptEngineManager().getEngineByName("rhino");
        plugin = this;
        getLogger().info("onEnable is called!");
        init();
        Events.fire(Trigger.enable);
    }
    @Override
    public void onDisable() {
        getLogger().info("onDisable is called!");
        Events.fire(Trigger.disable);
    }

    @Override
    public PluginCommand getCommand(@NotNull String name) {
        return super.getCommand(name);
    }
}