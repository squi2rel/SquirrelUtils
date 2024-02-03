package com.github.squi2rel.mcutils.useful;

import com.github.squi2rel.mcutils.Vars;
import com.github.squi2rel.mcutils.commands.BaseExecutor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

import static com.github.squi2rel.mcutils.Vars.config;

public class ChatFixer extends BaseExecutor implements Listener {
    private boolean enabled;

    public ChatFixer() {
        super("fixchat");
        enabled = config.getBoolean("fixchat");
        Bukkit.getPluginManager().registerEvents(this, Vars.plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("squirrel.admin.fixchat")) {
            enabled = !enabled;
            config.set("fixchat", enabled);
            sender.sendMessage(String.valueOf(enabled));
        }
        return true;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerChat(AsyncPlayerChatEvent e) {
        if (!enabled) return;
        Bukkit.getLogger().log(Level.INFO, "<" + e.getPlayer().getName() + "> " + e.getMessage());
    }
}
