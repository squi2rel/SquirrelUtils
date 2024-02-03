package com.github.squi2rel.mcutils.useful;

import com.github.squi2rel.mcutils.Vars;
import com.github.squi2rel.mcutils.commands.BaseExecutor;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEnderPearl;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

public class PearlLoadChunks extends BaseExecutor implements Listener {
    private static boolean enabled;
    private final ObjectLinkedOpenHashSet<EnderPearl> set = new ObjectLinkedOpenHashSet<>();
    public PearlLoadChunks() {
        super("pearlload");
        enabled = Vars.config.getBoolean("pearlloadchunks");
        Bukkit.getPluginManager().registerEvents(this, Vars.plugin);
        Bukkit.getScheduler().runTaskTimer(Vars.plugin, () -> {
            if (!enabled) return;
            Iterator<EnderPearl> iterator = set.iterator();
            while (iterator.hasNext()) {
                var p = ((CraftEnderPearl) iterator.next()).getHandle();
                if (p.isAlive()) {
                    var l = ((ServerLevel) p.level());
                    var cs = l.getChunkSource();
                    var c = p.chunkPosition();
                    cs.addRegionTicket(TicketType.PORTAL, c, 3, p.blockPosition());
                    l.getChunk(c.x, c.z);
                    Vec3 v = p.getDeltaMovement();
                    var block = new BlockPos((int) (p.getX() + v.x), (int) (p.getY() + v.y), (int) (p.getZ() + v.z));
                    var targetPos = new ChunkPos(block);
                    cs.addRegionTicket(TicketType.PORTAL, targetPos, 3, block);
                    l.getChunk(targetPos.x, targetPos.z);
                } else {
                    iterator.remove();
                }
            }
        }, 1, 1);
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        if (enabled && e.getEntityType() == EntityType.ENDER_PEARL) set.add((EnderPearl) e.getEntity());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("squirrel.admin.pearl")) {
            enabled = !enabled;
            Vars.config.set("pearlloadchunks", enabled);
            sender.sendMessage(String.valueOf(enabled));
        }
        return true;
    }
}
