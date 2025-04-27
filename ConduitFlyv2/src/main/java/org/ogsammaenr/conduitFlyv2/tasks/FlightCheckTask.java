package org.ogsammaenr.conduitFlyv2.tasks;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.ogsammaenr.conduitFlyv2.ConduitFlyv2;
import org.ogsammaenr.conduitFlyv2.manager.ConduitCache;
import org.ogsammaenr.conduitFlyv2.settings.RankSettings;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;
import org.ogsammaenr.conduitFlyv2.util.IslandUtils;

import java.util.Map;
import java.util.UUID;

public class FlightCheckTask implements Listener {

    private final ConduitFlyv2 plugin;
    private final ConduitCache conduitCache;
    private final RankSettingsManager rankSettingsManager;

    private static final double DEFAULT_RADIUS = 10.5; // Şu anda sabit 10 blok

    public FlightCheckTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.conduitCache = plugin.getConduitCache();
        this.rankSettingsManager = plugin.getRankSettingsManager();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        plugin.getLogger().info(player.getName() + " hareket etti!");

        // Konum değişikliği olup olmadığını kontrol et (Kafa çevirmeyi dikkate alma)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            // Eğer konum değişmediyse, yani oyuncu sadece kafa çevirdiyse, işlem yapma
            return;
        }

        if (shouldIgnore(player)) {
            plugin.getLogger().info(player.getName() + " shouldIgnore sonucu: true");
            return;
        }

        String permission = player.getEffectivePermissions().stream()
                .filter(perm -> perm.getPermission().startsWith("conduitfly."))
                .filter(perm -> perm.getValue()) // SADECE TRUE OLANLAR
                .map(perm -> perm.getPermission())
                .findFirst()
                .orElse("conduitfly.default");

        RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

        if (rankSettings != null) {
            double maxDistance = rankSettings.getRadius();

            plugin.getLogger().info(player.getName() + " için uçuş kontrolü yapılıyor. Mesafe: " + maxDistance);
            plugin.getLogger().info("Oyuncunun rütbesi: " + (rankSettings != null ? rankSettings.getPermission() : "Bulunamadı"));

            // Oyuncunun bulunduğu adada conduit var mı?
            if (!conduitCache.hasConduitInIsland(player)) {
                plugin.getLogger().info(player.getName() + " adasında conduit bulunamadı.");
                // Eğer ada yoksa uçuş iznini kapat
                if (player.getAllowFlight()) {
                    player.setAllowFlight(false);
                    player.setFlying(false);
                }
                return;
            }

            // Conduit alanına yakın mı kontrol et
            boolean nearConduit = conduitCache.isPlayerNearAnyConduit(player, maxDistance);  // Mesafeyi burada kontrol et
            plugin.getLogger().info(player.getName() + " conduit yakınlığı: " + nearConduit);
            if (nearConduit) {
                if (!player.getAllowFlight() && player.isOnGround()) {
                    player.setAllowFlight(true);
                    plugin.getFlightTimeTask().startFlight(player);
                }
            } else {
                player.setAllowFlight(false);
            }
        } else {
            player.sendMessage("rütbe ayarları alınamadığı için uçuş başlatılamadı");
        }
    }

    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();


        if (shouldIgnore(player)) {
            return;
        }

        // Eğer oyuncu zaten uçuyorsa, uçuşu tekrar başlatma
        Map<UUID, Long> flyingPlayers = plugin.getFlightTimeTask().getFlyingPlayers();
        if (flyingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        if (event.isFlying()) {
            // Oyuncu uçmaya başlıyor, ve yerden kalktıysa uçuş başlat

            plugin.getFlightTimeTask().startFlight(player);
            player.sendMessage("Your flight has started!");

        }
    }

    private boolean shouldIgnore(Player player) {
        if (player.hasPermission("conduitfly.bypassconduit")) {
            plugin.getLogger().info(player.getName() + " bypassconduit yetkisine sahip!");
            return true;
        }
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            plugin.getLogger().info(player.getName() + " uygun gamemode değil! Gamemode: " + player.getGameMode());
            return true;
        }
        if (!IslandUtils.isInBSkyBlockWorld(player)) {
            plugin.getLogger().info(player.getName() + " bskyblock dünyasında değil!");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        // Sadece oyuncular
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Eğer hasar tipi fall damage değilse geç
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        if (shouldIgnore(player)) {
            return;
        }

        // Eğer oyuncu conduit alanındaysa fall damage iptal
        if (plugin.getConduitCache().isPlayerNearAnyConduit(player, 10.5)) {
            event.setCancelled(true);
        }
    }
}

