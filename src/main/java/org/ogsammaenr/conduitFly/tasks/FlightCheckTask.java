package org.ogsammaenr.conduitFly.tasks;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.manager.ConduitCache;
import org.ogsammaenr.conduitFly.settings.RankSettings;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;
import org.ogsammaenr.conduitFly.util.IslandUtils;

import java.util.Map;
import java.util.UUID;

public class FlightCheckTask implements Listener {

    private final org.ogsammaenr.conduitFly.ConduitFly plugin;
    private final ConduitCache conduitCache;
    private final RankSettingsManager rankSettingsManager;

    /**************************************************************************************************************/
    //  Constructor metodu
    public FlightCheckTask(ConduitFly plugin) {
        this.plugin = plugin;
        this.conduitCache = plugin.getConduitCache();
        this.rankSettingsManager = plugin.getRankSettingsManager();
    }

    /**************************************************************************************************************/
    //  Oyuncu hareket ettiğinde çalışır
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        /*      Konum değişikliği olup olmadığını kontrol et (Kafa çevirmeyi dikkate alma)       */
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        /*      çeşitli kontroller      */
        if (shouldIgnore(player)) {
            return;
        }

        /*      oyuncunun adasında conduit var mı       */
        if (!conduitCache.hasConduitInIsland(player)) {
            if (player.getAllowFlight()) {
                player.setAllowFlight(false);
                player.setFlying(false);
            }
            return;
        }

        /*          ------perm kontrolü------            */

        String playerPermission = rankSettingsManager.getPermission(player);

        RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(playerPermission);

        /*      oyuncunun rütbesi varsa çalışır         */

        if (rankSettings != null) {
            double maxDistance = rankSettings.getRadius();

            /*      oyuncu conduite yeteri kadar yakın mı       */
            boolean nearConduit = conduitCache.isPlayerNearAnyConduit(player, maxDistance);
            if (nearConduit) {
                if (!player.getAllowFlight() && player.isOnGround()) {
                    plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                        if (player.isOnline() && player.isOnGround()) {
                            player.setAllowFlight(true);
                        }
                    }, 1L);
                } else if (plugin.getFlightTimeTask().getFlyingPlayers().containsKey(player.getUniqueId()) && player.isOnGround()) {
                    plugin.getFlightTimeTask().getFlyingPlayers().remove(player.getUniqueId());

                    String message = plugin.getMessageManager().getMessage("player-landed");
                    player.sendActionBar(message);
                }
            } else {
                player.setAllowFlight(false);
            }
        }
    }

    /**************************************************************************************************************/
    //  oyuncu uçmaya çalıştığında çalışır
    @EventHandler
    public void onPlayerToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();

        /*      çeşitli kontroller      */
        if (shouldIgnore(player)) {
            return;
        }

        /*      uçan oyuncuların verilerini getirir     */
        Map<UUID, Long> flyingPlayers = plugin.getFlightTimeTask().getFlyingPlayers();

        /*      oyuncu zaten uçuyorsa tekrar kontol etmez       */
        if (flyingPlayers.containsKey(player.getUniqueId())) {
            return;
        }

        /*      oyuncu gerçekten uçtuysa        */
        if (event.isFlying()) {
            long duration = rankSettingsManager.getRankSettingsByPermission(rankSettingsManager.getPermission(player)).getDuration();
            String message = plugin.getMessageManager().getMessage("flight-time").replace("%time%", Long.toString(duration));

            player.sendActionBar(message);
            /*      oyuncuyu uçan oyuncular listesine ekler     */
            plugin.getFlightTimeTask().startFlight(player);

        }
    }

    /**************************************************************************************************************/
    //  çeşitli kontroller
    private boolean shouldIgnore(Player player) {
        /*      oyuncunun bypassconduit permi var mı kontrol et     */
        if (player.hasPermission("conduitfly.bypassconduit")) {
            return true;
        }
        /*      oyuncunun oyun modu kontrol edilir      */
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            return true;
        }
        /*      oyuncunun bulunduğu dünya kontrol edilir        */
        if (!IslandUtils.isInBSkyBlockWorld(player)) {
            return true;
        }
        return false;
    }

    /**************************************************************************************************************/
    //  oyuncular fall damage aldığında çalışır
    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        /*      hasarı alan oyuncu mu       */
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }


        boolean preventFallDamage = rankSettingsManager.getRankSettingsByPermission(rankSettingsManager.getPermission(player)).shouldPreventFallDamage();
        if (!preventFallDamage) {
            return;
        }

        /*      hasarın tipi fall damage mi     */
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        /*      oyuncunun yetkisini getirir     */
        String playerRankPermission = rankSettingsManager.getPermission(player);
        RankSettings rankSettings = plugin.getRankSettingsManager().getRankSettingsByPermission(playerRankPermission);

        /*      oyuncunun yetkisi yoksa işlem yaptırmaz     */
        if (rankSettings == null) {
            return;
        }

        /*      oyuncu conduite yeteri kadar yakın mı       */
        double radius = rankSettings.getRadius();
        if (plugin.getConduitCache().isPlayerNearAnyConduit(player, radius)) {
            event.setCancelled(true);
        }
    }
}

