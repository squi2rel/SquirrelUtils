package com.github.squi2rel.mcutils.useful;

import com.github.squi2rel.mcutils.Vars;
import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftEnderPearl;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;

import java.util.Iterator;

public class PearlLoadChunks {
    private final ObjectLinkedOpenHashSet<EnderPearl> set = new ObjectLinkedOpenHashSet<>();
    public PearlLoadChunks() {
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onEntitySpawn(EntitySpawnEvent e) {
                if (e.getEntityType() == EntityType.ENDER_PEARL) set.add((EnderPearl) e.getEntity());
            }
        }, Vars.plugin);
        Bukkit.getScheduler().runTaskTimer(Vars.plugin, () -> {
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
}
