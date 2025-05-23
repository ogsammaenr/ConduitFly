package org.ogsammaenr.conduitFly.settings;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.ogsammaenr.conduitFly.ConduitFly;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RankSettingsManager {

    private final Map<String, RankSettings> rankSettingsMap;

    private ConduitFly plugin;

    /**************************************************************************************************************/
    //  constroctor Metodu
    public RankSettingsManager(FileConfiguration config, ConduitFly plugin) {
        this.rankSettingsMap = new HashMap<>();
        this.plugin = plugin;
        loadRankSettings(config);
    }

    /**************************************************************************************************************/
    // Belirli bir rankın özelliklerini döndürür
    public RankSettings getRankSettingsByPermission(String permission) {
        return rankSettingsMap.get(permission);
    }

    public Map<String, RankSettings> getRankSettingsMap() {
        return Collections.unmodifiableMap(rankSettingsMap);
    }

    /**************************************************************************************************************/
    // Config'den RankSettings verilerini yükler
    public void loadRankSettings(FileConfiguration config) {
        rankSettingsMap.clear();

        if (!config.contains("ranks")) return;

        Material conduitMaterial = Material.matchMaterial(config.getString("conduit.material"));
        if (conduitMaterial == null) {
            plugin.getLogger().warning("Invalid conduit material specified.");
        }

        int priorityCounter = 0;

        for (String rankKey : config.getConfigurationSection("ranks").getKeys(false)) {
            String base = "ranks." + rankKey + ".";
            String permission = config.getString(base + "permission");
            double radius = config.getDouble(base + "radius") + 0.5;
            long duration = config.getLong(base + "duration");
            boolean preventFall = config.getBoolean(base + "prevent-fall-damage");
            double price = config.getDouble(base + "price");
            String displayName = config.getString(base + "display-name");

            plugin.getLogger().info(permission + " radius : " + radius + " duration : " + duration + " preventfall : " + preventFall + " priority : " + priorityCounter);
            RankSettings rs = new RankSettings(permission, radius, duration, preventFall, price, displayName, priorityCounter++);
            rankSettingsMap.put(permission, rs);


        }
    }

    /**************************************************************************************************************/
    //  oyuncunun permini döndürür
    public String getPermission(Player player) {
        Optional<String> permission = player.getEffectivePermissions().stream()
                .filter(perm -> perm.getPermission().startsWith("conduitfly."))
                .filter(PermissionAttachmentInfo::getValue)     /* sadece aktif (true) olanlar */
                .map(PermissionAttachmentInfo::getPermission)
                .filter(rankSettingsMap::containsKey)           /* rank settings classında tanımlı olanlar*/
                .sorted((perm1, perm2) -> {                     /* büyükten küçüğe sırala*/
                    RankSettings rs1 = rankSettingsMap.get(perm1);
                    RankSettings rs2 = rankSettingsMap.get(perm2);
                    int p1 = rs1 != null ? rs1.getPriority() : Integer.MAX_VALUE;
                    int p2 = rs2 != null ? rs2.getPriority() : Integer.MAX_VALUE;
                    return Integer.compare(p2, p1);
                })
                .findFirst();

        return permission.orElse("conduitfly.default");
    }

    /**************************************************************************************************************/
}