package org.ogsammaenr.conduitFlyv2;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.ogsammaenr.conduitFlyv2.commands.MainCommand;
import org.ogsammaenr.conduitFlyv2.gui.RankUpgradeMenuListener;
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
    private FileConfiguration config;
    private PermissionManager permissionManager;
    private ConduitListener conduitListener;


    /**************************************************************************************************************/
    //  sdece sunucu başlarken çalışır diğer sınıflar bu metod sayesinde bir işe yarar
    @Override
    public void onEnable() {
        // Config dosyasını yükle
        saveDefaultConfig();
        this.config = getConfig();

        new PermissionManager(this).loadPermissions();

        // RankSettingsManager'ı başlat
        this.rankSettingsManager = new RankSettingsManager(config, this);

        this.conduitCache = new ConduitCache(this);
        this.conduitStorage = new ConduitStorage(this, conduitCache);
        this.flightTimeTask = new FlightTimeTask(this);
        this.permissionManager = new PermissionManager(this);
        this.conduitListener = new ConduitListener(this);


        /*  dünyalar yüklendikten sonra dosyadaki veriler belleğe yüklenir*/
        getServer().getScheduler().runTask(this, () -> {
            conduitStorage.loadFromYML();
            getLogger().info("*************Conduits loaded***************");
        });

        /*  diğer sınıfların kaydı yapılır  */
        PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(conduitListener, this);
        pm.registerEvents(new IslandEventListener(conduitCache, conduitStorage), this);
        pm.registerEvents(new FlightCheckTask(this), this);
        pm.registerEvents(new RankUpgradeMenuListener(this), this);
        flightTimeTask.runTaskTimer(this, 20L, 20L);

        getCommand("conduitfly").setExecutor(new MainCommand(this));

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
    //  plugini reloadlar
    public void reloadPlugin() {
        /*      config.yml yeniden yüklenir     */
        reloadConfig();

        /*      permler yeniden oluşturulur     */
        this.permissionManager = new PermissionManager(this);
        permissionManager.loadPermissions();

        /*      rütbe ayarları yeniden oluşturulur      */
        rankSettingsManager.loadRankSettings(getConfig());

        /*      conduit materyali configden alınır      */
        Material material = Material.matchMaterial(getConfig().getString("conduit.material"));

        /*      hata var mı yok mu kontrol edilir       */
        if (material == null) {
            getLogger().warning("Geçersiz conduit materyali bulundu! Default olarak CONDUIT kullanılacak.");
            material = Material.CONDUIT;
        }
        /*      conduit materyali güncellenir       */
        conduitListener.updateConduitMaterial(material);

        getLogger().info("ConduitFly ayarları başarıyla yeniden yüklendi!");
    }
}
