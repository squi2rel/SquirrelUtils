package com.github.squi2rel.mcutils.useful;

import com.github.squi2rel.mcutils.Vars;
import com.github.squi2rel.mcutils.commands.BaseExecutor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ImporterTopLevel;
import org.mozilla.javascript.Scriptable;

import java.util.Objects;

public class JS extends BaseExecutor implements Listener {
    public final Context cx;
    public final Scriptable scope;

    public JS() {
        super("js");
        cx = new ContextFactory().enterContext();
        cx.setOptimizationLevel(9);
        cx.setLanguageVersion(Context.VERSION_ES6);
        scope = new ImporterTopLevel(cx);
        Bukkit.getPluginManager().registerEvents(this, Vars.plugin);
    }

    private String getError(Throwable t) {
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    public String run(String text) {
        try {
            Object o = cx.evaluateString(scope, text, "js.js", 1, null);
            return Context.toString(o);
        } catch (Throwable t) {
            return getError(t);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player p && p.hasPermission("squirrel.admin.runjs")) {
            var i = new ItemStack(Material.WRITABLE_BOOK, 1);
            var meta = Objects.requireNonNull(i.getItemMeta());
            meta.setDisplayName("RUNJS");
            i.setItemMeta(meta);
            p.getInventory().addItem(i);
        } else {
            if (args.length == 0) return false;
            StringBuilder sb = new StringBuilder();
            for (String s : args) {
                sb.append(s).append(" ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sender.sendMessage(run(sb.toString()));
        }
        return true;
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (e.getPlayer().hasPermission("squirrel.admin.runjs") && e.getNewBookMeta().getDisplayName().equals("RUNJS")) {
            StringBuilder sb = new StringBuilder();
            for (String s : e.getNewBookMeta().getPages()) {
                sb.append(s);
            }
            e.getPlayer().sendMessage(run(sb.toString()));
        }
    }
}

