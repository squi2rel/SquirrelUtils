package com.github.squi2rel.mcutils.fun;

import arc.Events;
import com.github.squi2rel.mcutils.Vars;
import com.github.squi2rel.mcutils.commands.BaseExecutor;
import com.github.squi2rel.mcutils.events.Trigger;
import com.github.squi2rel.mcutils.utils.Func;
import com.github.squi2rel.mcutils.utils.Position;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Objects;

public class JB extends BaseExecutor implements Listener {
    ArrayList<JBData> jbs = new ArrayList<>();
    public JB() {
        super("jb");
        Bukkit.getScheduler().runTaskTimer(Vars.plugin, () -> {
            Iterator<JBData> iterator = jbs.iterator();
            while (iterator.hasNext()) {
                JBData jb = iterator.next();
                if (jb.player.isOnline() && jb.jb.isValid()) {
                    Position l = Position.from(jb.player.getLocation());
                    double d = jb.ss * Math.PI / 180;
                    ServerPlayer entity = ((CraftPlayer) jb.player).getHandle();
                    float bodyRot = getBodyRot(entity);
                    jb.jb.setHeadPose(jb.jb.getHeadPose().setX(d));
                    jb.jb.teleport(l.add(0, -0.25, 0).yaw(bodyRot));
                } else {
                    jb.jb.remove();
                    iterator.remove();
                }
            }
        }, 0L, 1L);
        Events.run(Trigger.disable, () -> {
            for (JBData jb : jbs) {
                jb.jb.remove();
            }
            jbs.clear();
        });
        Bukkit.getServer().getPluginManager().registerEvents(this, Vars.plugin);
    }

    //code from Minecraft
    private static float getBodyRot(ServerPlayer entity) {
        float bodyRot = entity.yBodyRot;
        float headRot = entity.yHeadRot;
        if (entity.isPassenger() && entity.getVehicle() instanceof LivingEntity vehicle) {
            bodyRot = vehicle.yBodyRot;
            float delta = headRot - bodyRot;
            float d2 = Mth.wrapDegrees(delta);
            if (d2 < -85.0F) {
                d2 = -85.0F;
            }

            if (d2 >= 85.0F) {
                d2 = 85.0F;
            }

            bodyRot = headRot - d2;
            if (d2 * d2 > 2500.0F) {
                bodyRot += d2 * 0.2F;
            }

        }
        return bodyRot;
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.isCancelled()) return;
        JBData d = Func.find(jbs, e -> e.jb == event.getEntity());
        if (d != null) {
            d.player.damage(event.getDamage(), event.getDamager());
            event.setCancelled(true);
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, String[] strings) {
        if (!(commandSender instanceof Player player)) return false;
        if (!player.hasPermission("squirrel.jb")) {
            player.sendMessage("权限不足");
            return false;
        }
        JBData d = Func.find(jbs, da -> da.player == player);
        if (d == null) {
            ArmorStand entity = (ArmorStand) player.getWorld().spawnEntity(player.getLocation().clone().subtract(0, -0.25, 0), EntityType.ARMOR_STAND, true);
            entity.setSmall(true);
            entity.setGravity(false);
            entity.setInvisible(true);
            entity.setInvulnerable(true);
            for (EquipmentSlot e : EquipmentSlot.values()) {
                entity.addEquipmentLock(e, ArmorStand.LockType.ADDING_OR_CHANGING);
            }
            Objects.requireNonNull(entity.getEquipment()).setHelmet(new ItemStack(Material.END_ROD, 1));
            jbs.add(new JBData(player, entity));
            player.sendMessage("§a已为你装上jb");
        } else {
            if (strings.length == 1) {
                switch (strings[0]) {
                    case "add" -> {
                        d.ss += 10;
                        player.sendMessage(String.valueOf(d.ss));
                        return true;
                    }
                    case "sub" -> {
                        d.ss -= 10;
                        player.sendMessage(String.valueOf(d.ss));
                        return true;
                    }
                }
            }
            d.jb.remove();
            jbs.remove(d);
            player.sendMessage("§a已为你取下jb");
        }
        return true;
    }

    static class JBData {
        Player player;
        ArmorStand jb;
        float ss;
        JBData(Player player, ArmorStand jb) {
            this.player = player;
            this.jb = jb;
            ss = 0;
        }
    }
}
