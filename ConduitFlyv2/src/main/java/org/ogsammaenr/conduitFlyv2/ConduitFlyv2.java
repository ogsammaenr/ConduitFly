package org.ogsammaenr.conduitFlyv2;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ogsammaenr.conduitFlyv2.listeners.ConduitListener;
import org.ogsammaenr.conduitFlyv2.listeners.IslandEventListener;
import org.ogsammaenr.conduitFlyv2.manager.ConduitCache;
import org.ogsammaenr.conduitFlyv2.manager.ConduitStorage;
import org.ogsammaenr.conduitFlyv2.manager.PermissionManager;
import org.ogsammaenr.conduitFlyv2.settings.RankSettingsManager;
import org.ogsammaenr.conduitFlyv2.tasks.FlightCheckTask;
import org.ogsammaenr.conduitFlyv2.tasks.FlightTimeTask;

public final class ConduitFlyv2 extends JavaPlugin {

    private ConduitCache conduitCache;
    private ConduitStorage conduitStorage;
    private FlightTimeTask flightTimeTask;
    private RankSettingsManager rankSettingsManager;


    /**************************************************************************************************************/
    //  sdece sunucu başlarken çalışır diğer sınıflar bu metod sayesinde bir işe yarar
    @Override
    public void onEnable() {
        // Config dosyasını yükle
        saveDefaultConfig();
        FileConfiguration config = getConfig();

        new PermissionManager(this).loadPermissions();

        // RankSettingsManager'ı başlat
        this.rankSettingsManager = new RankSettingsManager(config, this);

        this.conduitCache = new ConduitCache(this);
        this.conduitStorage = new ConduitStorage(this, conduitCache);
        this.flightTimeTask = new FlightTimeTask(this);

        /*  dünyalar yüklendikten sonra dosyadaki veriler belleğe yüklenir*/
        getServer().getScheduler().runTask(this, () -> {
            conduitStorage.loadFromYML();
            getLogger().info("*************Conduits loaded***************");
        });

        /*  diğer sınıfların kaydı yapılır  */
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new ConduitListener(this), this);
        pm.registerEvents(new IslandEventListener(conduitCache, conduitStorage), this);
        pm.registerEvents(new FlightCheckTask(this), this);
        flightTimeTask.runTaskTimer(this, 20L, 20L);

        /*  sunucunun başladığını konsolda belirtir  */
        getLogger().info("*******ConduitFlyv2 enabled!*******");

    }

    /**************************************************************************************************************/
    //  sadece sunucu kapanırken çalışır
    @Override
    public void onDisable() {
        /*  Sunucu kapatılırken cache'deki conduit verilerini kaydet  */
        if (conduitStorage != null) {
            conduitStorage.saveOnShutdown();
        }

        /*  pluginin düzgünce kapandığını konsola yaz*/
        getLogger().info("*******ConduitFlyv2 disabled!*******");
    }


    /**************************************************************************************************************/
    //  getterlar
    public ConduitCache getConduitCache() {
        return conduitCache;
    }

    public ConduitStorage getConduitStorage() {
        return conduitStorage;
    }

    public FlightTimeTask getFlightTimeTask() {
        return flightTimeTask;
    }

    public RankSettingsManager getRankSettingsManager() {
        return rankSettingsManager;
    }
    /**************************************************************************************************************/
}
