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

    /*      Uçan oyuncular için bir hashmap oluştur*/
    private final Map<UUID, Long> flyingPlayers = new HashMap<>();

    /**************************************************************************************************************/
    //  constructor methodu
    public FlightTimeTask(ConduitFlyv2 plugin) {
        this.plugin = plugin;
        this.rankSettingsManager = plugin.getRankSettingsManager();

    }

    /**************************************************************************************************************/
    /*      uçan oyuncuların verilerini döndürü     */
    public Map<UUID, Long> getFlyingPlayers() {
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
            player.sendMessage("§aUçuş başladı! Süre: " + duration + " saniye.");
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
                iterator.remove();
                player.sendMessage("yere indin süre sıfırlandı");
            }

            /*      oyuncunun permi alınır      */
            String permission = rankSettingsManager.getPermission(player);
            RankSettings rankSettings = rankSettingsManager.getRankSettingsByPermission(permission);

            /*      oyuncunun yetkisi var mı kontrol edilir*/
            if (rankSettings != null) {
                long maxDuration = rankSettings.getDuration();


                /*      oyuncunun süresi dolmuşsa uçuşu kapatılır verisi kaldırılır     */
                if (now - startTime >= maxDuration * 1000) {
                    if (player.isFlying()) {
                        player.setAllowFlight(false);
                        player.setFlying(false);
                        iterator.remove();
                        player.sendMessage("§cUçuş süreniz doldu, uçuş kapatıldı.");
                    }
                }
            }
        }
    }

    /**************************************************************************************************************/

}