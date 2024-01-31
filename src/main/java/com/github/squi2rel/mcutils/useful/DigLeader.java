package com.github.squi2rel.mcutils.useful;

import arc.files.Fi;
import com.github.squi2rel.mcutils.Vars;
import com.github.squi2rel.mcutils.utils.Sync;
import com.google.gson.JsonParser;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.Objective;

import java.util.Objects;
import java.util.UUID;

public class DigLeader implements Listener {
    public Object2IntLinkedOpenHashMap<UUID> map = new Object2IntLinkedOpenHashMap<>();
    private Objective dugList = null;
    public DigLeader() {
        Fi stats = new Fi(Objects.requireNonNull(Bukkit.getServer().getWorld("world")).getWorldFolder()).child("stats");
        if (stats.exists()) stats.walk(f -> {
            if (!f.extEquals("json")) return;
            String uuid = f.nameWithoutExtension();
            if (uuid.length() != 36) return;
            var mined = JsonParser.parseString(f.readString()).getAsJsonObject().get("stats").getAsJsonObject().get("minecraft:mined");
            if (mined == null) return;
            long dug = 0;
            var values = mined.getAsJsonObject().asMap().values();
            for (var v : values) dug += v.getAsLong();
            map.put(UUID.fromString(uuid), (int) Math.min(dug, Integer.MAX_VALUE));
        });
        loadToScoreBoard();
        Bukkit.getPluginManager().registerEvents(this, Vars.plugin);
    }

    public void loadToScoreBoard() {
        Sync.post(() -> {
            var sc = Objects.requireNonNull(Bukkit.getServer().getScoreboardManager()).getMainScoreboard();
            dugList = sc.getObjective("dug");
            if (dugList != null) dugList = sc.registerNewObjective("dug", Criteria.DUMMY, "挖掘榜");
            map.forEach((uuid, dug) -> dugList.getScore(Objects.requireNonNull(Bukkit.getOfflinePlayer(uuid).getName())).setScore(dug));
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.isCancelled()) return;
        var p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) return;
        var s = dugList.getScore(p.getName());
        s.setScore(map.addTo(p.getUniqueId(), 1));
    }
}
