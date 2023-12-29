package com.github.squi2rel.mcutils.commands;

import com.github.squi2rel.mcutils.Vars;
import org.bukkit.command.CommandExecutor;

import java.util.Objects;

public abstract class BaseExecutor implements CommandExecutor {
    public String name;
    public BaseExecutor(String name) {
        this.name = name;
        Objects.requireNonNull(Vars.plugin.getCommand(name)).setExecutor(this);
    }
}
