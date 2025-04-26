package org.ogsammaenr.conduitFlyv2.tasks;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.manager.ConduitCache;
import org.ogsammaenr.conduitFlyv2.util.IslandUtils;

public class FlightCheckTask implements Listener {

    private final ConduitFlyv2 plugin;
    private final ConduitCache conduitCache;

    private static final double DEFAULT_RADIUS = 10.5; // Şu anda sabit 10 blok

    public FlightCheckTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.conduitCache = plugin.getConduitCache();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Eğer oyuncu bypass iznine sahipse, geç
        if (player.hasPermission("conduitfly.bypassconduit")) {
            return;
        }

        // Sadece survival ve adventure modundaki oyuncuları kontrol et
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Sadece Skyblock dünyasında kontrol et
        if (!IslandUtils.isInBSkyBlockWorld(player)) {
            return;
        }

        // Konum değişikliği olup olmadığını kontrol et (Kafa çevirmeyi dikkate alma)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            // Eğer konum değişmediyse, yani oyuncu sadece kafa çevirdiyse, işlem yapma
            return;
        }

        // Oyuncunun bulunduğu adada conduit var mı?
        if (!conduitCache.hasConduitInIsland(player)) {
            // Eğer ada yoksa uçuş iznini kapat
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            return;
        }

        // Oyuncu conduit alanına yakın mı?
        boolean nearConduit = conduitCache.isPlayerNearAnyConduit(player, 10.5);  // Mesafeyi burada kontrol et

        if (nearConduit && !player.getAllowFlight()) {
            // Eğer alan içindeyse, uçuş iznini ver
            player.setAllowFlight(true);

        } else if (!nearConduit && player.getAllowFlight()) {
            // Eğer alan dışında ise, uçuş iznini kapat
            player.setAllowFlight(false);
            player.setFlying(false);
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        // Eğer oyuncu bypass iznine sahipse, kontrolü atla
        if (player.hasPermission("conduitfly.bypassconduit")) {
            return;
        }

        // Sadece survival ve adventure modunda olanları işleme al
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return;
        }

        // Sadece skyblock dünyasında olanları işleme al
        if (!IslandUtils.isInBSkyBlockWorld(player)) {
            return;
        }

        if (event.isFlying()) {
            // Oyuncu gerçekten uçmaya BAŞLADI
            plugin.getFlightTimeTask().startFlight(player);
        }
    }
}
