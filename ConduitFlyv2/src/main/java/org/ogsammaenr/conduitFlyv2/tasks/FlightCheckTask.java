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

    /**************************************************************************************************************/
    //  Constructor metodu
    public FlightCheckTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.conduitCache = plugin.getConduitCache();
        this.rankSettingsManager = plugin.getRankSettingsManager();
    }

    /**************************************************************************************************************/
    //  Oyuncu hareket ettiğinde çalışır
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        /*      çeşitli kontroller      */
        if (shouldIgnore(player)) {
            plugin.getLogger().info(player.getName() + " shouldIgnore sonucu: true");
            return;
        }

        /*      Konum değişikliği olup olmadığını kontrol et (Kafa çevirmeyi dikkate alma)       */
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }



        /*      oyuncunun adasında conduit var mı       */
        if (!conduitCache.hasConduitInIsland(player)) {
            plugin.getLogger().info(player.getName() + " adasında conduit bulunamadı.");
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

            /*      konsola gerekli bilgilendirmeleri gönderir      */
            plugin.getLogger().info(player.getName() + " için uçuş kontrolü yapılıyor. Mesafe: " + maxDistance);
            plugin.getLogger().info("Oyuncunun rütbesi: " + (rankSettings != null ? rankSettings.getPermission() : "Bulunamadı"));


            /*      oyuncu conduite yeteri kadar yakın mı       */
            boolean nearConduit = conduitCache.isPlayerNearAnyConduit(player, maxDistance);
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

            /*      oyuncuyu uçan oyuncular listesine ekler     */
            plugin.getFlightTimeTask().startFlight(player);
            player.sendMessage("Your flight has started!");

        }
    }

    /**************************************************************************************************************/
    //  çeşitli kontroller
    private boolean shouldIgnore(Player player) {
        /*      oyuncunun bypassconduit permi var mı kontrol et     */
        if (player.hasPermission("conduitfly.bypassconduit")) {
            plugin.getLogger().info(player.getName() + " bypassconduit yetkisine sahip!");
            return true;
        }
        /*      oyuncunun oyun modu kontrol edilir      */
        if (player.getGameMode() != GameMode.SURVIVAL && player.getGameMode() != GameMode.ADVENTURE) {
            plugin.getLogger().info(player.getName() + " uygun gamemode değil! Gamemode: " + player.getGameMode());
            return true;
        }
        /*      oyuncunun bulunduğu dünya kontrol edilir        */
        if (!IslandUtils.isInBSkyBlockWorld(player)) {
            plugin.getLogger().info(player.getName() + " bskyblock dünyasında değil!");
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

        /*      hasarın tipi fall damage mi     */
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) {
            return;
        }

        /*      çeşitli kontroller      */
        if (shouldIgnore(player)) {
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
            if (rankSettings.shouldPreventFallDamage()) {
                event.setCancelled(true);
            }
        }
    }
}

