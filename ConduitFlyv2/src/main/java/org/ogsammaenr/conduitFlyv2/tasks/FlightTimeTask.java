package org.ogsammaenr.conduitFlyv2.tasks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class FlightTimeTask extends BukkitRunnable {

    private final ConduitFlyv2 plugin;

    private final Map<UUID, Long> flyingPlayers = new HashMap<>();
    private final long flightDuration;

    public FlightTimeTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        // Config'ten uçuş süresi alınıyor
        this.flightDuration = plugin.getConfig().getLong("flight-time.duration", 10) * 1000; // saniye cinsinden alınıyor, ms'ye çevriliyor
    }

    // Uçuş başlatma
    public void startFlight(Player player) {
        flyingPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        player.sendMessage("§aUçuş başladı! Süre: " + (flightDuration / 1000) + " saniye.");
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

            // Eğer belirli bir süre geçmişse uçuşu kapat
            if (now - startTime >= flightDuration) {
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