package org.ogsammaenr.conduitFly.tasks;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.ogsammaenr.conduitFly.ConduitFly;
import org.ogsammaenr.conduitFly.settings.RankSettings;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class FlightTimeTask extends BukkitRunnable {

    private final ConduitFly plugin;
    private final RankSettingsManager rankSettingsManager;

    /*      Uçan oyuncular için bir hashmap oluştur*/
    private final ConcurrentHashMap<UUID, Long> flyingPlayers = new ConcurrentHashMap<>();

    /**************************************************************************************************************/
    //  constructor methodu
    public FlightTimeTask(ConduitFly plugin) {
        this.plugin = plugin;
        this.rankSettingsManager = plugin.getRankSettingsManager();

    }

    /**************************************************************************************************************/
    /*      uçan oyuncuların verilerini döndürü     */
    public ConcurrentHashMap<UUID, Long> getFlyingPlayers() {
        return flyingPlayers;
    }

    /**************************************************************************************************************/
    //  oyuncuların verisini kaydetmek için yardımcı method
    public void startFlight(Player player) {
        String permission = rankSettingsManager.getPermission(player);
        RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

        if (rankSettings != null) {
            long duration = rankSettings.getDuration();

            flyingPlayers.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }

    /**************************************************************************************************************/
    //  her saniye uçan oyuncuları kontrol eder
    @Override
    public void run() {
        long now = System.currentTimeMillis();

        /*      uçan oyuncuları teker teker döner       */
        for (Iterator<Map.Entry<UUID, Long>> iterator = flyingPlayers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<UUID, Long> entry = iterator.next();
            UUID uuid = entry.getKey();
            long startTime = entry.getValue();

            Player player = Bukkit.getPlayer(uuid);
            /*      oyuncu bulunmadıysa ya da oyuncu çıkmışsa verisi silinir        */
            if (player == null || !player.isOnline()) {
                iterator.remove();
                continue;
            }
            /*      oyuncu yere inmişse verisi silinir*/
            if (player.isOnGround()) {
                String message = plugin.getMessageManager().getMessage("flight.player-landed");
                player.sendActionBar(message);

                iterator.remove();

            }

            /*      oyuncunun permi alınır      */
            String permission = rankSettingsManager.getPermission(player);
            RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

            /*      oyuncunun yetkisi var mı kontrol edilir*/
            if (rankSettings != null) {
                long maxDuration = rankSettings.getDuration();


                /*      oyuncunun süresi dolmuşsa uçuşu kapatılır verisi kaldırılır     */
                if (now - startTime >= maxDuration * 1000) {
                    String message = plugin.getMessageManager().getMessage("flight.time-expired");
                    player.sendActionBar(message);
                    iterator.remove();
                    if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                        continue;
                    }
                    player.setAllowFlight(false);
                    player.setFlying(false);

                } else {
                    long duration = ((maxDuration * 1000 - (now - startTime)) / 1000);
                    String message = plugin.getMessageManager().getMessage("flight.time-left").replace("%time%", Long.toString(duration));
                    player.sendActionBar(message);
                }
            }
        }
    }

    /**************************************************************************************************************/

}