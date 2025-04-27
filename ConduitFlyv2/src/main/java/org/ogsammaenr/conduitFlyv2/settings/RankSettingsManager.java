package org.ogsammaenr.conduitFlyv2.settings;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;

import java.util.HashMap;
import java.util.Map;

public class RankSettingsManager {

    private final Map<String, RankSettings> rankSettingsMap;

    private ConduitFlyv2 plugin;

    public RankSettingsManager(FileConfiguration config, ConduitFlyv2 plugin) {
        this.rankSettingsMap = new HashMap<>();
        this.plugin = plugin;
        loadRankSettings(config);
    }

    // Permission'a göre RankSettings'i al
    public RankSettings getRankSettingsByPermission(String permission) {
        return rankSettingsMap.get(permission);
    }

    // Config'den RankSettings verilerini yükler
    private void loadRankSettings(FileConfiguration config) {
        if (!config.contains("ranks")) return;

        Material conduitMaterial = Material.matchMaterial(config.getString("conduit.material"));
        if (conduitMaterial == null) {
            plugin.getLogger().warning("Geçersiz conduit materyali belirtildi.");
        }

        for (String rankKey : config.getConfigurationSection("ranks").getKeys(false)) {
            String base = "ranks." + rankKey + ".";
            String permission = config.getString(base + "permission");
            double radius = config.getDouble(base + "radius");
            long duration = config.getLong(base + "duration");
            boolean preventFall = config.getBoolean(base + "preventFallDamage");

            plugin.getLogger().info(permission + "rankı : " + radius + " " + duration + " " + preventFall);
            RankSettings rs = new RankSettings(permission, radius, duration, preventFall);
            rankSettingsMap.put(permission, rs);


        }
    }
}