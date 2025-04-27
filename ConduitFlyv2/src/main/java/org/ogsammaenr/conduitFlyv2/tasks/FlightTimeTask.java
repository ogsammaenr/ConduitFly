package org.ogsammaenr.conduitFlyv2.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.settings.RankSettings;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FlightTimeTask extends BukkitRunnable {

    private final ConduitFlyv2 plugin;
    private final RankSettingsManager rankSettingsManager;

    private final Map<UUID, Long> flyingPlayers = new HashMap<>();


    public FlightTimeTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.rankSettingsManager = plugin.getRankSettingsManager();

    }

    public Map<UUID, Long> getFlyingPlayers() {
        return flyingPlayers;
    }

    // Uçuş başlatma
    public void startFlight(Player player) {
        String permission = player.getEffectivePermissions().stream()
                .filter(perm -> perm.getPermission().startsWith("conduitfly."))
                .filter(perm -> perm.getValue()) // SADECE TRUE OLANLAR
                .map(perm -> perm.getPermission())
                .findFirst()
                .orElse("conduitfly.default");

        RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

        if (rankSettings != null) {
            long duration = rankSettings.getDuration();

            flyingPlayers.put(player.getUniqueId(), System.currentTimeMillis());
            player.sendMessage("§aUçuş başladı! Süre: " + duration + " saniye.");
        }
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        for (Iterator<Map.Entry<UUID, Long>> iterator = flyingPlayers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<UUID, Long> entry = iterator.next();
            UUID uuid = entry.getKey();
            long startTime = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }
            if (player.isOnGround()) {
                iterator.remove();
                player.sendMessage("yere indin süre sıfırlandı");
            }

            String permission = player.getEffectivePermissions().stream()
                    .filter(perm -> perm.getPermission().startsWith("conduitfly."))
                    .filter(perm -> perm.getValue()) // SADECE TRUE OLANLAR
                    .map(perm -> perm.getPermission())
                    .findFirst()
                    .orElse("conduitfly.default");

            RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

            if (rankSettings != null) {
                long maxDuration = rankSettings.getDuration();


                // Eğer belirli bir süre geçmişse uçuşu kapat
                if (now - startTime >= maxDuration * 1000) {
                    if (player.isFlying()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        iterator.remove(); // uçuş bitince oyuncu listeden çıkarılır
                        player.sendMessage("§cUçuş süreniz doldu, uçuş kapatıldı.");
                    }
                }
            }
        }
    }

}