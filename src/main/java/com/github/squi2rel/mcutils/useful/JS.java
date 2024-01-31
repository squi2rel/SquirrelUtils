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
import rhino.*;
import rhino.module.RequireBuilder;

import java.util.Objects;

public class JS extends BaseExecutor implements Listener {
    public final Context context;
    public final Scriptable scope;

    public JS() {
        super("js");
        context = getScriptContext();
        scope = new ImporterTopLevel(context);
        new RequireBuilder().setSandboxed(true).createRequire(context, scope).install(scope);
        Bukkit.getPluginManager().registerEvents(this, Vars.plugin);
    }

    private String getError(Throwable t) {
        return t.getClass().getSimpleName() + (t.getMessage() == null ? "" : ": " + t.getMessage());
    }

    public String runConsole(String text) {
        try{
            Object o = context.evaluateString(scope, text, "console.js", 1);
            if (o instanceof NativeJavaObject n) o = n.unwrap();
            if (o == null) o = "null";
            else if (o instanceof Undefined) o = "undefined";
            var out = o.toString();
            return out == null ? "null" : out;
        } catch (Throwable t) {
            return getError(t);
        }
    }

    private Context getScriptContext(){
        Context context = Context.getCurrentContext();
        if(context == null) context = Context.enter();
        context.setOptimizationLevel(9);
        return context;
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
            sender.sendMessage(runConsole(sb.toString()));
        }
        return true;
    }

    @EventHandler
    public void onPlayerEditBook(PlayerEditBookEvent e) {
        if (e.getNewBookMeta().getDisplayName().equals("RUNJS")) {
            StringBuilder sb = new StringBuilder();
            for (String s : e.getNewBookMeta().getPages()) {
                sb.append(s);
            }
            e.getPlayer().sendMessage(runConsole(sb.toString()));
        }
    }
}
