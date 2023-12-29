package com.github.squi2rel.mcutils.fun;

import com.github.squi2rel.mcutils.commands.BaseExecutor;
import com.github.squi2rel.mcutils.utils.Http;
import com.github.squi2rel.mcutils.utils.Sync;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_20_R2.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;

import static com.github.squi2rel.mcutils.Vars.economy;
import static net.minecraft.world.level.material.MapColor.MATERIAL_COLORS;

public class ImageToMap extends BaseExecutor {
    private static PaletteData[] colors;
    private static final RGB c1 = new RGB(), c2 = new RGB(), c3 = new RGB();

    public ImageToMap() {
        super("img2map");
        ArrayList<PaletteData> tmp = new ArrayList<>();
        MapColor.Brightness[] brightnesses = MapColor.Brightness.values();
        for (int i = 1; i < MATERIAL_COLORS.length; i++) {
            MapColor color = MATERIAL_COLORS[i];
            if (color == null) continue;
            for (MapColor.Brightness b : brightnesses) {
                int abgr = color.calculateRGBColor(b);
                tmp.add(new PaletteData(new RGB(abgr & 255, abgr >> 8 & 255, abgr >> 16 & 255), color, b));
            }
        }
        colors = tmp.toArray(new PaletteData[0]);
    }

    private static int trans(RGB co1, RGB co2, int mul) {
        return co1.add(c1.set(co2).mul(mul).mv(4)).rgb();
    }

    private static ImageToMapData img2map(BufferedImage image) {
        int height = image.getHeight(), width = image.getWidth();
        //图像转换
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB pix = c2.setRGB(image.getRGB(x, y));
                RGB nearest = findNearestColor(pix);
                image.setRGB(x, y, nearest.rgb());
                pix.sub(c1.set(nearest));
                if (x + 1 < width) {
                    image.setRGB(x + 1, y, trans(c3.setRGB(image.getRGB(x + 1, y)), pix, 7));
                }
                if (y + 1 < height) {
                    if (x - 1 > 0) {
                        image.setRGB(x - 1, y + 1, trans(c3.setRGB(image.getRGB(x - 1, y + 1)), pix, 3));
                    }
                    image.setRGB(x, y + 1, trans(c3.setRGB(image.getRGB(x, y + 1)), pix, 5));
                    if (x + 1 < width) {
                        image.setRGB(x + 1, y + 1, trans(c3.setRGB(image.getRGB(x + 1, y + 1)), pix, 1));
                    }
                }
            }
        }
        //图像转地图数据
        int tw = (int) Math.ceil(width / 128d), th = (int) Math.ceil(height / 128d);
        ImageToMapData data = new ImageToMapData(tw, th);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                RGB rgb = findNearestColor(c2.setRGB(image.getRGB(x, y)));
                int pw = (int) Math.floor(x / 128d);
                int ph = (int) Math.floor(y / 128d);
                data.data[pw + ph * tw][x % 128 + (y % 128) * 128] = (byte) (rgb.data.brightness.id | rgb.data.map.id << 2);
            }
        }
        return data;
    }

    private static RGB findNearestColor(RGB color) {
        int max = 255 * 255 + 255 * 255 + 255 * 255 + 1;
        RGB output = color;
        for (PaletteData i : colors) {
            int delta = c1.set(color).sub(i.rgb).pow();
            if (delta < max) {
                max = delta;
                output = i.rgb;
            }
        }
        return output;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return false;
        if (!player.hasPermission("squirrel.img2map")) {
            player.sendMessage("权限不足");
            return false;
        }
        if (args.length < 1) {
            player.sendMessage("参数错误! 用法: /img2map <url> [scale]");
            return false;
        }
        ServerPlayer p = ((CraftPlayer) player).getHandle();
        try {
            float scale = args.length > 1 ? Float.parseFloat(args[1]) : 1;
            Http.get(args[0]).timeout(10000).error(e -> Sync.post(() -> player.sendMessage("图片无效"))).submit(r -> {
                long time = new Date().getTime();
                try {
                    BufferedImage i = ImageIO.read(r.getResultAsStream());
                    int w = i.getWidth(), h = i.getHeight();
                    if (scale != 1) {
                        w = (int) (w * scale);
                        h = (int) (h * scale);
                    }
                    if (w > 1280 || h > 1280) {
                        Sync.post(() -> player.sendMessage("图片太大"));
                        return;
                    }
                    if (scale != 1) {
                        BufferedImage newImage = new BufferedImage(w, h, i.getType());
                        for (int x = 0; x < w; x++) {
                            for (int y = 0; y < h; y++) {
                                newImage.setRGB(x, y, i.getRGB((int) (x / scale), (int) (y / scale)));
                            }
                        }
                        i = newImage;
                    }
                    int ww = w;
                    int hh = h;
                    BufferedImage image = i;
                    Sync.post(() -> {
                        OfflinePlayer op = Bukkit.getServer().getOfflinePlayer(player.getUniqueId());
                        if (economy != null && !economy.has(op, ww * hh / 100d)) {
                            player.sendMessage("金钱不足 需要" + ww * hh / 100d);
                            return;
                        }
                        ImageToMapData m = img2map(image);
                        for (byte[] b : m.data) {
                            ItemStack itemstack = new ItemStack(Items.FILLED_MAP);
                            net.minecraft.world.level.Level l = p.level();
                            int id = l.getFreeMapId();
                            MapItemSavedData data = MapItemSavedData.createFresh(0d, 0d, (byte) 0, false, false, l.dimension());
                            data.locked = true;
                            data.colors = b;
                            l.setMapData(MapItem.makeKey(id), data);
                            itemstack.getOrCreateTag().putInt("map", id);
                            if (!p.getInventory().add(itemstack.copy())) {
                                p.drop(itemstack, false);
                            }
                        }
                        if (economy != null) {
                            economy.withdrawPlayer(op, ww * hh / 100d);
                            player.sendMessage("你已花费金钱" + economy.format(ww * hh / 100d));
                        }
                        player.sendMessage("输出大小: " + m.width + "x" + m.height);
                    });
                } catch (Exception e) {
                    Sync.post(() -> player.sendMessage("图片无效"));
                }
                Bukkit.getLogger().log(Level.INFO, "Costs: " + (new Date().getTime() - time) + "ms");
            });
        } catch (NumberFormatException e) {
            player.sendMessage("缩放无效");
        } catch (Exception e) {
            player.sendMessage("图片无效");
        }
        return true;
    }

    private static class PaletteData {
        RGB rgb;
        MapColor map;
        MapColor.Brightness brightness;

        PaletteData(RGB rgb, MapColor map, MapColor.Brightness brightness) {
            this.rgb = rgb;
            this.map = map;
            this.brightness = brightness;
            rgb.data = this;
        }
    }

    private static class RGB {
        PaletteData data;
        int r, g, b;

        RGB(int r, int g, int b) {
            this.r = r;
            this.g = g;
            this.b = b;
        }

        RGB() {
            this(0, 0, 0);
        }

        public RGB set(RGB c) {
            r = c.r;
            g = c.g;
            b = c.b;
            return this;
        }

        public RGB setRGB(int rgb) {
            r = rgb >> 16 & 0xff;
            g = rgb >> 8 & 0xff;
            b = rgb & 0xff;
            return this;
        }

        public RGB sub(RGB c) {
            r = r - c.r;
            g = g - c.g;
            b = b - c.b;
            return this;
        }

        public RGB add(RGB c) {
            r = Math.max(Math.min(c.r + r, 255), 0);
            g = Math.max(Math.min(c.g + g, 255), 0);
            b = Math.max(Math.min(c.b + b, 255), 0);
            return this;
        }

        public RGB mul(int m) {
            r *= m;
            g *= m;
            b *= m;
            return this;
        }

        public RGB mv(int s) {
            r >>= s;
            g >>= s;
            b >>= s;
            return this;
        }

        public int pow() {
            return r * r + g * g + b * b;
        }

        public int rgb() {
            return r << 16 | g << 8 | b;
        }
    }

    static class ImageToMapData {
        int width;
        int height;
        byte[][] data;

        ImageToMapData(int w, int h) {
            width = w;
            height = h;
            data = new byte[w * h][];
            for (int i = 0, c = w * h; i < c; i++) data[i] = new byte[16384];
        }
    }
}
