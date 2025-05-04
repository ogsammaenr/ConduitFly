package org.ogsammaenr.conduitFly;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.ogsammaenr.conduitFly.commands.CommandTabCompleter;
import org.ogsammaenr.conduitFly.commands.MainCommand;
import org.ogsammaenr.conduitFly.gui.RankUpgradeMenuListener;
import org.ogsammaenr.conduitFly.listeners.ConduitListener;
import org.ogsammaenr.conduitFly.listeners.IslandEventListener;
import org.ogsammaenr.conduitFly.manager.ConduitCache;
import org.ogsammaenr.conduitFly.manager.ConduitStorage;
import org.ogsammaenr.conduitFly.manager.MessageManager;
import org.ogsammaenr.conduitFly.manager.PermissionManager;
import org.ogsammaenr.conduitFly.settings.RankSettingsManager;
import org.ogsammaenr.conduitFly.tasks.FlightCheckTask;
import org.ogsammaenr.conduitFly.tasks.FlightTimeTask;

public final class ConduitFly extends JavaPlugin {

    private ConduitCache conduitCache;
    private ConduitStorage conduitStorage;
    private FlightTimeTask flightTimeTask;
    private RankSettingsManager rankSettingsManager;
    private FileConfiguration config;
    private PermissionManager permissionManager;
    private ConduitListener conduitListener;
    private MessageManager messageManager;

    private static Economy economy;


    /**************************************************************************************************************/
    //  sdece sunucu başlarken çalışır diğer sınıflar bu metod sayesinde bir işe yarar
    @Override
    public void onEnable() {
        // Config dosyasını yükle
        saveDefaultConfig();
        this.config = getConfig();

        new PermissionManager(this).loadPermissions();

        if (!setupEconomy()) {
            getLogger().severe("Vault or a compatible economy plugin is missing!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // RankSettingsManager'ı başlat
        this.rankSettingsManager = new RankSettingsManager(config, this);

        this.conduitCache = new ConduitCache(this);
        this.conduitStorage = new ConduitStorage(this, conduitCache);
        this.flightTimeTask = new FlightTimeTask(this);
        this.permissionManager = new PermissionManager(this);
        this.conduitListener = new ConduitListener(this);
        this.messageManager = new MessageManager(this);


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
        getCommand("conduitfly").setTabCompleter(new CommandTabCompleter());

        /*  sunucunun başladığını konsolda belirtir  */
        getLogger().info("*******ConduitFly enabled!*******");

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
        getLogger().info("*******ConduitFly disabled!*******");
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

    public Economy getEconomy() {
        return economy;
    }

    public MessageManager getMessageManager() {
        return messageManager;
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
            getLogger().warning("Invalid Conduit Material Provided. Using default : CONDUIT");
            material = Material.CONDUIT;
        }
        /*      conduit materyali güncellenir       */
        conduitListener.updateConduitMaterial(material);

        getLogger().info("ConduitFly settings reloaded successfully!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) return false;
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }
}
